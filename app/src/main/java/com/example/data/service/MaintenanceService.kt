package com.example.data.service

import com.example.data.auth.AuthCredentialsConfig
import com.example.data.auth.UserSessionManager
import com.example.data.model.EmergencyMode
import com.example.data.model.EmergencySystemState
import com.example.data.model.FamilyMemberAccount
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MaintenanceService using Firebase Realtime Database (with local persistent fallback)
 * to monitor the emergency status flags (Emergency Mode 1, 2, or 3), custom maintenance
 * duration in hours, selective disabled features, and RRR Family Member credentials,
 * updating the app's global UI state across all screens in real time.
 */
class MaintenanceService(
    private val sessionManager: UserSessionManager
) {
    private val _emergencyState = MutableStateFlow(
        EmergencySystemState(
            activeMode = sessionManager.getEmergencyMode(),
            maintenanceReason = sessionManager.getMaintenanceReason(),
            customDurationHours = sessionManager.getCustomDurationHours(),
            expiresAtTimestamp = sessionManager.getEmergencyExpiresAt(),
            disabledFeatureIds = sessionManager.getDisabledFeatureIds()
        )
    )
    val emergencyState: StateFlow<EmergencySystemState> = _emergencyState.asStateFlow()

    private val _familyMembers = MutableStateFlow<List<FamilyMemberAccount>>(
        sessionManager.getFamilyMembers()
    )
    val familyMembers: StateFlow<List<FamilyMemberAccount>> = _familyMembers.asStateFlow()

    private var maintenanceRef: DatabaseReference? = null
    private var familyMembersRef: DatabaseReference? = null
    private var maintenanceListener: ValueEventListener? = null
    private var familyListener: ValueEventListener? = null

    init {
        attachRealtimeListeners()
    }

    private fun attachRealtimeListeners() {
        try {
            val db = FirebaseDatabase.getInstance()
            val mRef = db.getReference("maintenance_status")
            val fRef = db.getReference("rrr_family_members")
            maintenanceRef = mRef
            familyMembersRef = fRef

            val mListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        pushEmergencyStateToRealtimeDb(_emergencyState.value)
                        return
                    }
                    try {
                        val modeCode = snapshot.child("modeCode").getValue(Int::class.java)
                        val modeName = snapshot.child("activeMode").getValue(String::class.java)
                        val mode = when {
                            modeCode != null -> EmergencyMode.fromCode(modeCode)
                            !modeName.isNullOrBlank() -> try {
                                EmergencyMode.valueOf(modeName)
                            } catch (_: Exception) {
                                EmergencyMode.NORMAL
                            }
                            else -> _emergencyState.value.activeMode
                        }
                        val reason = snapshot.child("maintenanceReason").getValue(String::class.java)
                            ?: _emergencyState.value.maintenanceReason
                        val durationHours = snapshot.child("customDurationHours").getValue(Int::class.java)
                            ?: _emergencyState.value.customDurationHours
                        val expiresAt = snapshot.child("expiresAtTimestamp").getValue(Long::class.java)
                            ?: _emergencyState.value.expiresAtTimestamp
                        val updatedBy = snapshot.child("lastUpdatedBy").getValue(String::class.java)
                            ?: "Rizwan RR"
                        val updatedAt = snapshot.child("lastUpdatedAt").getValue(Long::class.java)
                            ?: System.currentTimeMillis()

                        val disabledSet = mutableSetOf<String>()
                        val disabledNode = snapshot.child("disabledFeatureIds")
                        if (disabledNode.exists()) {
                            for (child in disabledNode.children) {
                                val idVal = child.getValue(String::class.java)
                                if (!idVal.isNullOrBlank()) {
                                    disabledSet.add(idVal.trim().lowercase())
                                }
                            }
                        } else {
                            disabledSet.addAll(_emergencyState.value.disabledFeatureIds)
                        }

                        sessionManager.saveEmergencyMode(mode)
                        sessionManager.saveMaintenanceReason(reason)
                        sessionManager.saveCustomDurationHours(durationHours)
                        sessionManager.saveDisabledFeatureIds(disabledSet)

                        _emergencyState.value = EmergencySystemState(
                            activeMode = mode,
                            maintenanceReason = reason,
                            customDurationHours = durationHours.coerceAtLeast(1),
                            expiresAtTimestamp = expiresAt,
                            lastUpdatedBy = updatedBy,
                            lastUpdatedAt = updatedAt,
                            disabledFeatureIds = disabledSet
                        )
                    } catch (_: Exception) {}
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            mRef.addValueEventListener(mListener)
            maintenanceListener = mListener

            val fListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        pushFamilyMembersToRealtimeDb(_familyMembers.value)
                        return
                    }
                    try {
                        val loaded = mutableListOf<FamilyMemberAccount>()
                        for (child in snapshot.children) {
                            val id = child.child("id").getValue(String::class.java) ?: continue
                            val password = child.child("password").getValue(String::class.java) ?: ""
                            val memberName = child.child("memberName").getValue(String::class.java) ?: "RRR Family Member"
                            val details = child.child("details").getValue(String::class.java) ?: "Verified RRR Family Member"
                            val createdAt = child.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis()
                            val lastLoginAt = child.child("lastLoginAt").getValue(Long::class.java) ?: 0L
                            val loginCount = child.child("loginCount").getValue(Int::class.java) ?: 0
                            val isActive = child.child("isActive").getValue(Boolean::class.java) ?: true
                            loaded.add(
                                FamilyMemberAccount(
                                    id = id,
                                    password = password,
                                    memberName = memberName,
                                    details = details,
                                    createdAt = createdAt,
                                    lastLoginAt = lastLoginAt,
                                    loginCount = loginCount,
                                    isActive = isActive
                                )
                            )
                        }
                        if (loaded.isNotEmpty()) {
                            sessionManager.saveFamilyMembers(loaded)
                            _familyMembers.value = loaded
                        }
                    } catch (_: Exception) {}
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            fRef.addValueEventListener(fListener)
            familyListener = fListener
        } catch (_: Exception) {
            // Firebase Realtime Database not initialized in local unit test or offline mode;
            // local StateFlow + SharedPreferences remain authoritative.
        }
    }

    fun updateEmergencyMode(
        mode: EmergencyMode,
        reason: String,
        customDurationHours: Int = _emergencyState.value.customDurationHours
    ) {
        val safeHours = customDurationHours.coerceIn(1, 720)
        val now = System.currentTimeMillis()
        val expiresAt = now + (safeHours * 3_600_000L)

        sessionManager.saveEmergencyMode(mode)
        sessionManager.saveMaintenanceReason(reason)
        sessionManager.saveCustomDurationHours(safeHours)

        val newState = _emergencyState.value.copy(
            activeMode = mode,
            maintenanceReason = reason,
            customDurationHours = safeHours,
            expiresAtTimestamp = expiresAt,
            lastUpdatedBy = "Rizwan RR",
            lastUpdatedAt = now
        )
        _emergencyState.value = newState
        pushEmergencyStateToRealtimeDb(newState)
    }

    fun updateCustomDurationHours(hours: Int) {
        val safeHours = hours.coerceIn(1, 720)
        val now = System.currentTimeMillis()
        val expiresAt = now + (safeHours * 3_600_000L)
        sessionManager.saveCustomDurationHours(safeHours)

        val newState = _emergencyState.value.copy(
            customDurationHours = safeHours,
            expiresAtTimestamp = expiresAt,
            lastUpdatedAt = now
        )
        _emergencyState.value = newState
        pushEmergencyStateToRealtimeDb(newState)
    }

    fun updateDisabledFeatures(disabledIds: Set<String>) {
        val cleanIds = disabledIds.map { it.trim().lowercase() }.toSet()
        sessionManager.saveDisabledFeatureIds(cleanIds)
        val newState = _emergencyState.value.copy(
            disabledFeatureIds = cleanIds,
            lastUpdatedAt = System.currentTimeMillis()
        )
        _emergencyState.value = newState
        pushEmergencyStateToRealtimeDb(newState)
    }

    fun createOrUpdateFamilyMember(
        id: String,
        password: String,
        memberName: String,
        details: String
    ): FamilyMemberAccount {
        val cleanId = id.trim()
        val cleanPass = password.trim()
        val cleanName = memberName.trim().ifEmpty { cleanId }
        val cleanDetails = details.trim().ifEmpty { "Verified RRR Family Member" }
        val now = System.currentTimeMillis()

        val currentList = _familyMembers.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id.equals(cleanId, ignoreCase = true) }
        val updatedAccount = if (existingIndex >= 0) {
            val prev = currentList[existingIndex]
            prev.copy(
                id = cleanId,
                password = cleanPass,
                memberName = cleanName,
                details = cleanDetails,
                isActive = true
            ).also { currentList[existingIndex] = it }
        } else {
            FamilyMemberAccount(
                id = cleanId,
                password = cleanPass,
                memberName = cleanName,
                details = cleanDetails,
                createdAt = now,
                lastLoginAt = 0L,
                loginCount = 0,
                isActive = true
            ).also { currentList.add(0, it) }
        }

        sessionManager.saveFamilyMembers(currentList)
        _familyMembers.value = currentList
        pushFamilyMembersToRealtimeDb(currentList)
        return updatedAccount
    }

    fun toggleFamilyMemberActive(id: String) {
        val currentList = _familyMembers.value.map { member ->
            if (member.id.equals(id.trim(), ignoreCase = true)) {
                member.copy(isActive = !member.isActive)
            } else {
                member
            }
        }
        sessionManager.saveFamilyMembers(currentList)
        _familyMembers.value = currentList
        pushFamilyMembersToRealtimeDb(currentList)
    }

    fun deleteFamilyMember(id: String) {
        if (id.trim().equals("admin", ignoreCase = true)) return // Protect primary master family account
        val currentList = _familyMembers.value.filterNot { it.id.equals(id.trim(), ignoreCase = true) }
        sessionManager.saveFamilyMembers(currentList)
        _familyMembers.value = currentList
        pushFamilyMembersToRealtimeDb(currentList)
    }

    /**
     * Authenticates a Family Member using Realtime Database credentials or Master Family credentials.
     * Automatically updates loginCount and lastLoginAt upon successful login.
     */
    fun authenticateFamilyMember(id: String, password: String): FamilyMemberAccount? {
        val cleanId = id.trim()
        val cleanPass = password.trim()
        if (cleanId.isEmpty() || cleanPass.isEmpty()) return null

        val currentList = _familyMembers.value.toMutableList()
        val matchedIndex = currentList.indexOfFirst { account ->
            account.isActive &&
                account.id.equals(cleanId, ignoreCase = true) &&
                account.password == cleanPass
        }

        if (matchedIndex >= 0) {
            val matched = currentList[matchedIndex]
            val updated = matched.copy(
                lastLoginAt = System.currentTimeMillis(),
                loginCount = matched.loginCount + 1
            )
            currentList[matchedIndex] = updated
            sessionManager.saveFamilyMembers(currentList)
            _familyMembers.value = currentList
            pushFamilyMembersToRealtimeDb(currentList)
            return updated
        }

        // Also check PBKDF2 master credentials ("admin" / "Rexlify")
        if (AuthCredentialsConfig.isValidFamilyCredentials(cleanId, cleanPass)) {
            val now = System.currentTimeMillis()
            val adminIdx = currentList.indexOfFirst { it.id.equals(cleanId, ignoreCase = true) }
            val updated = if (adminIdx >= 0) {
                val prev = currentList[adminIdx]
                prev.copy(lastLoginAt = now, loginCount = prev.loginCount + 1).also {
                    currentList[adminIdx] = it
                }
            } else {
                FamilyMemberAccount(
                    id = cleanId,
                    password = cleanPass,
                    memberName = "RRR Family Member",
                    details = "Official RRR Family Account",
                    createdAt = now,
                    lastLoginAt = now,
                    loginCount = 1,
                    isActive = true
                ).also { currentList.add(0, it) }
            }
            sessionManager.saveFamilyMembers(currentList)
            _familyMembers.value = currentList
            pushFamilyMembersToRealtimeDb(currentList)
            return updated
        }

        return null
    }

    private fun pushEmergencyStateToRealtimeDb(state: EmergencySystemState) {
        try {
            val payload = mapOf(
                "modeCode" to state.activeMode.code,
                "activeMode" to state.activeMode.name,
                "maintenanceReason" to state.maintenanceReason,
                "customDurationHours" to state.customDurationHours,
                "expiresAtTimestamp" to state.expiresAtTimestamp,
                "lastUpdatedBy" to state.lastUpdatedBy,
                "lastUpdatedAt" to state.lastUpdatedAt,
                "disabledFeatureIds" to state.disabledFeatureIds.toList()
            )
            maintenanceRef?.setValue(payload)
        } catch (_: Exception) {}
    }

    private fun pushFamilyMembersToRealtimeDb(members: List<FamilyMemberAccount>) {
        try {
            val map = members.associate { member ->
                val safeKey = member.id.replace(Regex("[.#$\\[\\]/]"), "_")
                safeKey to mapOf(
                    "id" to member.id,
                    "password" to member.password,
                    "memberName" to member.memberName,
                    "details" to member.details,
                    "createdAt" to member.createdAt,
                    "lastLoginAt" to member.lastLoginAt,
                    "loginCount" to member.loginCount,
                    "isActive" to member.isActive
                )
            }
            familyMembersRef?.setValue(map)
        } catch (_: Exception) {}
    }

    fun cleanup() {
        try {
            maintenanceListener?.let { maintenanceRef?.removeEventListener(it) }
            familyListener?.let { familyMembersRef?.removeEventListener(it) }
        } catch (_: Exception) {}
    }
}
