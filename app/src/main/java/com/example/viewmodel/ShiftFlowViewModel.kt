package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.SeedShiftFlowData
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ShiftFlowUiState(
    val workspaceMode: WorkspaceMode = WorkspaceMode.EMPLOYEE_MOBILE,
    val activeMobileTab: EmployeeMobileTab = EmployeeMobileTab.SCHEDULE,
    val activeScheduleSubTab: EmployeeScheduleSubTab = EmployeeScheduleSubTab.MY_SHIFTS,
    val activeManagerSection: ManagerWebSection = ManagerWebSection.ROSTER,

    // Active Store & Persona
    val locations: List<StoreLocation> = SeedShiftFlowData.locations,
    val selectedLocationId: String = "main_st",
    val selectedJobSiteFilter: String = "All Job Sites",
    val activeEmployeeId: String = "emp_maria",
    val selectedWeekLabel: String = "Week of Nov 16 – 22",
    val selectedDayStripIndex: Int = 0, // 0..6

    // Sub-screens / Modals
    val showCallOutScreenForShift: ShiftItem? = null,
    val showFindCoverageScreen: Boolean = false,
    val showSwapModalForShift: ShiftItem? = null,
    val showRolesPermissionsSubScreen: Boolean = false,

    // Roster & Shifts
    val shifts: List<ShiftItem> = SeedShiftFlowData.initialShifts,
    val isRosterPublished: Boolean = false,
    val shiftTemplates: List<ShiftTemplate> = SeedShiftFlowData.shiftTemplates,
    val scheduleTemplates: List<ScheduleTemplate> = SeedShiftFlowData.scheduleTemplates,

    // Availability & Time Off
    val mariaAvailability: List<DayAvailability> = SeedShiftFlowData.initialMariaAvailability,
    val isRecurringAvailabilityMode: Boolean = false,
    val gridAvailability: Map<String, Map<Int, Boolean>> = SeedShiftFlowData.employeeGridAvailability,
    val timeOffRequests: List<TimeOffRequest> = SeedShiftFlowData.initialTimeOffRequests,
    val availabilitySubmittedCount: Int = 9,

    // Coverage & Alerts
    val activeCalledOutShiftTitle: String = "Today · 2:00 – 10:00 PM · Scooper",
    val activeCalledOutSub: String = "Maria R. called out at 8:40 AM",
    val coverageCandidates: List<CoverageCandidate> = SeedShiftFlowData.initialCoverageCandidates,
    val alerts: List<AlertItem> = SeedShiftFlowData.initialAlerts,

    // Employees, Skills & Roles
    val employees: List<Employee> = SeedShiftFlowData.employees,
    val customSkills: List<CustomSkill> = SeedShiftFlowData.customSkills,
    val rolePermissions: List<RolePermissionItem> = SeedShiftFlowData.rolePermissions,
    val employeeSearchQuery: String = "",
    val selectedRoleFilter: String = "All roles",
    val selectedStatusFilter: String = "All statuses",

    // WorkChat & Notifications
    val chatMessages: List<ChatMessage> = SeedShiftFlowData.initialChatMessages,
    val notificationPrefs: List<NotificationPrefItem> = SeedShiftFlowData.initialNotificationPrefs,
    val quietHoursEnabled: Boolean = true,
    val quietHoursStart: String = "10:00 PM",
    val quietHoursEnd: String = "7:00 AM",

    // Banner / Toast feedback
    val statusBannerMessage: String? = null
) {
    val currentLocation: StoreLocation
        get() = locations.find { it.id == selectedLocationId } ?: locations.first()

    val activeEmployee: Employee
        get() = employees.find { it.id == activeEmployeeId } ?: employees[2]
}

class ShiftFlowViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ShiftFlowUiState())
    val uiState: StateFlow<ShiftFlowUiState> = _uiState.asStateFlow()

    fun setWorkspaceMode(mode: WorkspaceMode) {
        _uiState.update {
            it.copy(
                workspaceMode = mode,
                showCallOutScreenForShift = null,
                showFindCoverageScreen = false
            )
        }
    }

    fun selectMobileTab(tab: EmployeeMobileTab) {
        _uiState.update {
            it.copy(
                activeMobileTab = tab,
                showCallOutScreenForShift = null,
                showFindCoverageScreen = false
            )
        }
    }

    fun selectScheduleSubTab(subTab: EmployeeScheduleSubTab) {
        _uiState.update { it.copy(activeScheduleSubTab = subTab) }
    }

    fun selectManagerSection(section: ManagerWebSection) {
        _uiState.update {
            it.copy(
                activeManagerSection = section,
                showRolesPermissionsSubScreen = false
            )
        }
    }

    fun selectLocation(locationId: String) {
        _uiState.update {
            it.copy(
                selectedLocationId = locationId,
                selectedJobSiteFilter = "All Job Sites",
                statusBannerMessage = "Switched store location to ${it.locations.find { loc -> loc.id == locationId }?.name ?: "Store"}"
            )
        }
    }

    fun selectJobSiteFilter(jobSite: String) {
        _uiState.update { it.copy(selectedJobSiteFilter = jobSite) }
    }

    fun selectDayStripIndex(index: Int) {
        _uiState.update { it.copy(selectedDayStripIndex = index) }
    }

    fun clearBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    // --- Call Out Sick & Coverage Flow (PDF Pages 2, 3, 4, 5) ---

    fun openCallOutScreen(shift: ShiftItem) {
        _uiState.update { it.copy(showCallOutScreenForShift = shift) }
    }

    fun closeCallOutScreen() {
        _uiState.update { it.copy(showCallOutScreenForShift = null) }
    }

    fun confirmCallOut(shift: ShiftItem, reason: CallOutReason, managerNote: String) {
        _uiState.update { state ->
            val updatedShifts = state.shifts.map {
                if (it.id == shift.id) it.copy(
                    status = ShiftStatus.CALLED_OUT,
                    hasNote = managerNote.isNotBlank(),
                    noteText = managerNote.ifBlank { reason.label }
                ) else it
            }
            val newAlert = AlertItem(
                id = "alert_call_out_${System.currentTimeMillis()}",
                title = "Call-out logged (${reason.label})",
                subtitle = "${shift.startTime} – ${shift.endTime} · Manager notified & finding coverage",
                timeAgo = "Just now"
            )
            state.copy(
                shifts = updatedShifts,
                showCallOutScreenForShift = null,
                activeCalledOutShiftTitle = "Today · ${shift.startTime} – ${shift.endTime} · ${shift.skillRequired}",
                activeCalledOutSub = "${shift.employeeShortName ?: "Maria R."} called out (${reason.label})",
                alerts = listOf(newAlert) + state.alerts,
                statusBannerMessage = "Manager notified right away. Auto-ranking qualified teammates to cover this shift."
            )
        }
    }

    fun openFindCoverageScreen(find: Boolean = true) {
        _uiState.update { it.copy(showFindCoverageScreen = find) }
    }

    fun assignCoverageCandidate(candidateId: String) {
        _uiState.update { state ->
            val candidate = state.coverageCandidates.find { it.employeeId == candidateId } ?: return@update state
            val updatedCandidates = state.coverageCandidates.map {
                if (it.employeeId == candidateId) it.copy(status = CandidateInviteStatus.ASSIGNED)
                else it
            }
            val updatedShifts = state.shifts.map { shift ->
                if (shift.id == "shift_today_maria") {
                    shift.copy(
                        employeeId = candidate.employeeId,
                        employeeShortName = candidate.name.substringAfter(". "),
                        employeeInitials = candidate.initials,
                        status = ShiftStatus.PUBLISHED
                    )
                } else shift
            }
            state.copy(
                coverageCandidates = updatedCandidates,
                shifts = updatedShifts,
                statusBannerMessage = "Shift assigned to ${candidate.name.substringAfter(". ")}!"
            )
        }
    }

    fun sendCoverRequestToNext3() {
        _uiState.update { state ->
            val updatedCandidates = state.coverageCandidates.map {
                if (it.status == CandidateInviteStatus.READY) it.copy(status = CandidateInviteStatus.ASKED)
                else it
            }
            state.copy(
                coverageCandidates = updatedCandidates,
                statusBannerMessage = "Push + SMS cover request sent to top 3 ranked teammates! First to accept gets the shift."
            )
        }
    }

    // --- Urgent Alert Accept / Decline (PDF Page 5) ---

    fun respondToUrgentCoverAlert(alertId: String, accept: Boolean) {
        _uiState.update { state ->
            val updatedAlerts = state.alerts.map { alert ->
                if (alert.id == alertId) {
                    alert.copy(
                        isResolved = true,
                        resolutionLabel = if (accept) "Shift Accepted · Added to your schedule (20 h total)" else "Declined"
                    )
                } else alert
            }
            val extraShift = if (accept) {
                listOf(
                    ShiftItem(
                        id = "shift_covered_${System.currentTimeMillis()}",
                        locationId = "main_st",
                        locationName = "Main St store",
                        jobSite = "Scoop Station",
                        employeeId = state.activeEmployeeId,
                        employeeShortName = state.activeEmployee.shortName,
                        employeeFullName = state.activeEmployee.fullName,
                        employeeInitials = state.activeEmployee.initials,
                        avatarBgHex = state.activeEmployee.avatarBgHex,
                        dayIndex = 5,
                        dayHeader = "Sat, Nov 14",
                        dayAbbrev = "SAT",
                        dateNum = "14",
                        startTime = "2:00 PM",
                        endTime = "10:00 PM",
                        compactTime = "2 – 10 PM",
                        durationHours = 8,
                        shiftLabel = "Covered Shift",
                        skillRequired = "Scooper",
                        status = ShiftStatus.PUBLISHED
                    )
                )
            } else emptyList()

            state.copy(
                alerts = updatedAlerts,
                shifts = extraShift + state.shifts,
                statusBannerMessage = if (accept) "You accepted the 2:00 – 10:00 PM shift! Roster updated automatically." else "Cover request declined."
            )
        }
    }

    // --- Shift Swap, Offer, Drop & Claim Open Shift ---

    fun openSwapModal(shift: ShiftItem?) {
        _uiState.update { it.copy(showSwapModalForShift = shift) }
    }

    fun executeShiftAction(shift: ShiftItem, actionType: String, targetTeammateName: String?) {
        _uiState.update { state ->
            val autoApprove = state.currentLocation.autoApproveQualifiedSwaps
            val updatedShifts = state.shifts.map { current ->
                if (current.id == shift.id) {
                    when (actionType) {
                        "DROP_TO_OPEN" -> current.copy(
                            employeeId = null,
                            employeeShortName = null,
                            employeeFullName = null,
                            employeeInitials = "OPEN",
                            status = ShiftStatus.OPEN,
                            hasNote = true,
                            noteText = "Dropped by ${shift.employeeShortName ?: "Maria R."}"
                        )
                        "SWAP_DIRECT" -> current.copy(
                            employeeShortName = if (autoApprove && targetTeammateName != null) targetTeammateName else current.employeeShortName,
                            status = if (autoApprove) ShiftStatus.PUBLISHED else ShiftStatus.SWAP_REQUESTED,
                            hasNote = true,
                            noteText = if (autoApprove) "Auto-swapped with $targetTeammateName" else "Swap offered to $targetTeammateName"
                        )
                        else -> current.copy(status = ShiftStatus.SWAP_REQUESTED)
                    }
                } else current
            }
            val banner = when (actionType) {
                "DROP_TO_OPEN" -> "Shift dropped to Open Shifts pool for qualified staff to pick up."
                "SWAP_DIRECT" -> if (autoApprove) "Shift swap with ${targetTeammateName ?: "Priya S."} auto-approved!" else "Swap request sent for approval."
                else -> "Shift offered to qualified teammates."
            }
            state.copy(
                shifts = updatedShifts,
                showSwapModalForShift = null,
                statusBannerMessage = banner
            )
        }
    }

    fun claimOpenShift(shiftId: String) {
        _uiState.update { state ->
            val emp = state.activeEmployee
            val updatedShifts = state.shifts.map { shift ->
                if (shift.id == shiftId) {
                    shift.copy(
                        employeeId = emp.id,
                        employeeShortName = emp.shortName,
                        employeeFullName = emp.fullName,
                        employeeInitials = emp.initials,
                        avatarBgHex = emp.avatarBgHex,
                        status = ShiftStatus.PUBLISHED,
                        hasNote = true,
                        noteText = "Claimed by ${emp.shortName} (Auto-approved)"
                    )
                } else shift
            }
            state.copy(
                shifts = updatedShifts,
                statusBannerMessage = "Open shift claimed! Added to ${emp.shortName}'s schedule."
            )
        }
    }

    // --- Availability & Time Off (PDF Page 1) ---

    fun selectAvailabilitySlot(dayIndex: Int, slot: AvailabilitySlot) {
        _uiState.update { state ->
            val updated = state.mariaAvailability.map {
                if (it.dayIndex == dayIndex) it.copy(selectedSlot = slot) else it
            }
            // Also sync to Manager Grid availability for Maria R.
            val mariaGrid = state.gridAvailability["emp_maria"]?.toMutableMap() ?: mutableMapOf()
            mariaGrid[dayIndex] = (slot != AvailabilitySlot.OFF)
            val updatedGrid = state.gridAvailability.toMutableMap().apply {
                put("emp_maria", mariaGrid)
            }
            state.copy(
                mariaAvailability = updated,
                gridAvailability = updatedGrid
            )
        }
    }

    fun copyLastWeekAvailability() {
        _uiState.update { state ->
            state.copy(
                mariaAvailability = SeedShiftFlowData.initialMariaAvailability,
                statusBannerMessage = "Copied last week's availability pattern."
            )
        }
    }

    fun clearAllAvailability() {
        _uiState.update { state ->
            val cleared = state.mariaAvailability.map { it.copy(selectedSlot = AvailabilitySlot.OFF) }
            state.copy(
                mariaAvailability = cleared,
                statusBannerMessage = "Cleared all availability selections for this week."
            )
        }
    }

    fun toggleRecurringAvailabilityMode(isRecurring: Boolean) {
        _uiState.update { it.copy(isRecurringAvailabilityMode = isRecurring) }
    }

    fun submitAvailability() {
        _uiState.update { state ->
            val modeText = if (state.isRecurringAvailabilityMode) "Recurring availability saved!" else "Availability submitted for ${state.selectedWeekLabel}!"
            state.copy(
                availabilitySubmittedCount = 10,
                statusBannerMessage = "$modeText Synced with Manager Roster Grid."
            )
        }
    }

    fun addTimeOffRequest(typeLabel: String, dateRange: String, reason: String, isRecurring: Boolean) {
        if (dateRange.isBlank()) return
        _uiState.update { state ->
            val autoApprove = state.currentLocation.autoApproveTimeOffUnder24h
            val newReq = TimeOffRequest(
                id = "to_${System.currentTimeMillis()}",
                employeeId = state.activeEmployee.id,
                employeeName = state.activeEmployee.shortName,
                typeLabel = typeLabel,
                dateRange = dateRange,
                reason = reason.ifBlank { "Personal schedule preference" },
                status = if (autoApprove) RequestStatus.APPROVED else RequestStatus.PENDING,
                isRecurringUnavailability = isRecurring
            )
            state.copy(
                timeOffRequests = listOf(newReq) + state.timeOffRequests,
                statusBannerMessage = if (autoApprove) "$typeLabel for $dateRange automatically approved!" else "$typeLabel submitted for manager review."
            )
        }
    }

    fun updateTimeOffStatus(requestId: String, status: RequestStatus) {
        _uiState.update { state ->
            val updated = state.timeOffRequests.map {
                if (it.id == requestId) it.copy(status = status) else it
            }
            state.copy(
                timeOffRequests = updated,
                statusBannerMessage = "Request status updated to ${status.label}."
            )
        }
    }

    // --- Manager Web Roster Builder (PDF Page 7) ---

    fun assignShiftFromRosterCell(employee: Employee, dayIndex: Int, template: ShiftTemplate) {
        _uiState.update { state ->
            val dayHeaders = listOf("Mon, Nov 16", "Tue, Nov 17", "Wed, Nov 18", "Thu, Nov 19", "Fri, Nov 20", "Sat, Nov 21", "Sun, Nov 22")
            val dayAbbrevs = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
            val dateNums = listOf("16", "17", "18", "19", "20", "21", "22")

            // Remove existing shift for this employee & day if replacing
            val filtered = state.shifts.filterNot {
                it.locationId == "main_st" && it.employeeId == employee.id && it.dayIndex == dayIndex
            }
            val newShift = ShiftItem(
                id = "shift_${employee.id}_${dayIndex}_${System.currentTimeMillis()}",
                locationId = "main_st",
                locationName = "Main St store",
                jobSite = template.jobSite,
                employeeId = employee.id,
                employeeShortName = employee.shortName,
                employeeFullName = employee.fullName,
                employeeInitials = employee.initials,
                avatarBgHex = employee.avatarBgHex,
                dayIndex = dayIndex,
                dayHeader = dayHeaders.getOrElse(dayIndex) { "Mon, Nov 16" },
                dayAbbrev = dayAbbrevs.getOrElse(dayIndex) { "MON" },
                dateNum = dateNums.getOrElse(dayIndex) { "16" },
                startTime = template.startTime,
                endTime = template.endTime,
                compactTime = template.compactTime,
                durationHours = template.durationHours,
                shiftLabel = template.title.substringAfter(" "),
                skillRequired = template.requiredSkill,
                status = if (state.isRosterPublished) ShiftStatus.PUBLISHED else ShiftStatus.DRAFT
            )
            state.copy(
                shifts = filtered + newShift,
                statusBannerMessage = "Assigned ${template.compactTime} (${template.requiredSkill}) to ${employee.shortName}."
            )
        }
    }

    fun removeShiftFromRosterCell(shiftId: String) {
        _uiState.update { state ->
            state.copy(
                shifts = state.shifts.filterNot { it.id == shiftId },
                statusBannerMessage = "Shift removed from roster."
            )
        }
    }

    fun autoFillRosterFromAvailability() {
        _uiState.update { state ->
            val currentShifts = state.shifts.toMutableList()
            // Fill Fri evening gap for Jordan T. (Available on Fri index 4) and Thu morning for Noah B. (Available on Thu index 3)
            val jordanFriExists = currentShifts.any { it.employeeId == "emp_jordan" && it.dayIndex == 4 && it.locationId == "main_st" }
            if (!jordanFriExists) {
                currentShifts.add(
                    ShiftItem(
                        id = "autofill_jordan_fri",
                        locationId = "main_st",
                        locationName = "Main St store",
                        jobSite = "Scoop Station",
                        employeeId = "emp_jordan",
                        employeeShortName = "Jordan T.",
                        employeeFullName = "Jordan Taylor",
                        employeeInitials = "JT",
                        avatarBgHex = 0xFFDDF7EC,
                        dayIndex = 4,
                        dayHeader = "Fri, Nov 20",
                        dayAbbrev = "FRI",
                        dateNum = "20",
                        startTime = "2:00 PM",
                        endTime = "10:00 PM",
                        compactTime = "2 – 10 PM",
                        durationHours = 8,
                        shiftLabel = "Scooper",
                        skillRequired = "Scooper",
                        status = ShiftStatus.DRAFT
                    )
                )
            }
            val noahMonExists = currentShifts.any { it.employeeId == "emp_noah" && it.dayIndex == 0 && it.locationId == "main_st" }
            if (!noahMonExists) {
                currentShifts.add(
                    ShiftItem(
                        id = "autofill_noah_mon",
                        locationId = "main_st",
                        locationName = "Main St store",
                        jobSite = "Scoop Station",
                        employeeId = "emp_noah",
                        employeeShortName = "Noah B.",
                        employeeFullName = "Noah Brooks",
                        employeeInitials = "NB",
                        avatarBgHex = 0xFFE2E8F0,
                        dayIndex = 0,
                        dayHeader = "Mon, Nov 16",
                        dayAbbrev = "MON",
                        dateNum = "16",
                        startTime = "2:00 PM",
                        endTime = "7:00 PM",
                        compactTime = "2 – 7 PM",
                        durationHours = 5,
                        shiftLabel = "Scooper",
                        skillRequired = "Scooper",
                        status = ShiftStatus.DRAFT
                    )
                )
            }
            state.copy(
                shifts = currentShifts,
                statusBannerMessage = "Auto-filled open shifts from availability while respecting Ava K.'s 16h minor limit!"
            )
        }
    }

    fun requestMissingAvailability() {
        _uiState.update { state ->
            state.copy(
                statusBannerMessage = "Push & SMS availability reminders sent to Noah B. and Chloe P."
            )
        }
    }

    fun publishRoster() {
        _uiState.update { state ->
            val publishedShifts = state.shifts.map {
                if (it.status == ShiftStatus.DRAFT) it.copy(status = ShiftStatus.PUBLISHED) else it
            }
            val pubAlert = AlertItem(
                id = "alert_pub_${System.currentTimeMillis()}",
                title = "Roster published for ${state.selectedWeekLabel}",
                subtitle = "All team members notified via Push & Email · Just now",
                timeAgo = "Just now"
            )
            state.copy(
                shifts = publishedShifts,
                isRosterPublished = true,
                alerts = listOf(pubAlert) + state.alerts,
                statusBannerMessage = "Roster for ${state.selectedWeekLabel} published! Push & email alerts sent to all scheduled staff."
            )
        }
    }

    fun applyScheduleTemplate(template: ScheduleTemplate) {
        _uiState.update { state ->
            state.copy(
                statusBannerMessage = "Applied '${template.name}' (${template.shiftsCount} shifts, ${template.totalWeeklyHours} hrs)."
            )
        }
    }

    // --- Employees, Custom Skills & Roles (PDF Pages 8 & 9) ---

    fun updateEmployeeSearch(query: String) {
        _uiState.update { it.copy(employeeSearchQuery = query) }
    }

    fun updateRoleFilter(role: String) {
        _uiState.update { it.copy(selectedRoleFilter = role) }
    }

    fun updateStatusFilter(status: String) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
    }

    fun toggleRolesPermissionsScreen(show: Boolean) {
        _uiState.update { it.copy(showRolesPermissionsSubScreen = show) }
    }

    fun inviteEmployee(fullName: String, role: SystemRole, skillName: String, contact: String, isMinor: Boolean) {
        if (fullName.isBlank()) return
        val parts = fullName.trim().split(" ")
        val shortName = if (parts.size > 1) "${parts.first()} ${parts.last().first().uppercase()}." else fullName
        val initials = parts.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "NW" }
        _uiState.update { state ->
            val newEmp = Employee(
                id = "emp_${System.currentTimeMillis()}",
                shortName = shortName,
                fullName = fullName,
                initials = initials,
                avatarBgHex = 0xFFFFE6DF,
                role = role,
                primaryPosition = skillName,
                skills = listOf(skillName, "Crew Member"),
                contact = contact.ifBlank { "(555) 010-1999" },
                status = EmployeeStatus.INVITED,
                scheduledHours = 0,
                isMinor = isMinor,
                age = if (isMinor) 16 else null
            )
            state.copy(
                employees = state.employees + newEmp,
                statusBannerMessage = "Invitation sent to $fullName ($skillName)!"
            )
        }
    }

    fun addCustomSkill(skillName: String, category: String) {
        if (skillName.isBlank()) return
        _uiState.update { state ->
            val newSkill = CustomSkill(
                id = "skill_${System.currentTimeMillis()}",
                name = skillName.trim(),
                category = category.ifBlank { "Custom" },
                colorHex = 0xFF132238,
                bgHex = 0xFFFFE6DF,
                activeStaffCount = 1
            )
            state.copy(
                customSkills = state.customSkills + newSkill,
                statusBannerMessage = "Added custom skill '${skillName.trim()}' to store roles."
            )
        }
    }

    // --- WorkChat & Notification Preferences (PDF Page 6 & Messaging Screenshot) ---

    fun sendChatMessage(text: String, isManagerBroadcast: Boolean = false) {
        if (text.isBlank()) return
        _uiState.update { state ->
            val sender = if (state.workspaceMode == WorkspaceMode.MANAGER_WEB || isManagerBroadcast) {
                "CSC Columbia Management"
            } else {
                state.activeEmployee.fullName
            }
            val msg = ChatMessage(
                id = "msg_${System.currentTimeMillis()}",
                locationId = state.selectedLocationId,
                senderName = sender,
                senderRole = if (isManagerBroadcast) "Manager" else state.activeEmployee.primaryPosition,
                initials = if (isManagerBroadcast) "CM" else state.activeEmployee.initials,
                timestamp = "Just now",
                text = text.trim(),
                isManagerBroadcast = isManagerBroadcast || state.workspaceMode == WorkspaceMode.MANAGER_WEB
            )
            state.copy(chatMessages = state.chatMessages + msg)
        }
    }

    fun toggleNotificationChannel(prefId: String, channel: String) {
        _uiState.update { state ->
            val updated = state.notificationPrefs.map { pref ->
                if (pref.id == prefId) {
                    when (channel) {
                        "PUSH" -> pref.copy(pushEnabled = !pref.pushEnabled)
                        "EMAIL" -> pref.copy(emailEnabled = !pref.emailEnabled)
                        "TEXT" -> pref.copy(textEnabled = !pref.textEnabled)
                        else -> pref
                    }
                } else pref
            }
            state.copy(notificationPrefs = updated)
        }
    }

    fun toggleQuietHours(enabled: Boolean) {
        _uiState.update { it.copy(quietHoursEnabled = enabled) }
    }

    fun updateWeekStartDay(weekStartDay: WeekStartDay) {
        _uiState.update { state ->
            val updatedLocations = state.locations.map { loc ->
                if (loc.id == state.selectedLocationId) loc.copy(weekStartDay = weekStartDay) else loc
            }
            state.copy(
                locations = updatedLocations,
                statusBannerMessage = "Week starting day updated to ${weekStartDay.label}."
            )
        }
    }

    fun updateStoreAutomation(autoSwaps: Boolean, autoTimeOff: Boolean, reminderMins: Int) {
        _uiState.update { state ->
            val updatedLocations = state.locations.map { loc ->
                if (loc.id == state.selectedLocationId) {
                    loc.copy(
                        autoApproveQualifiedSwaps = autoSwaps,
                        autoApproveTimeOffUnder24h = autoTimeOff,
                        shiftReminderMinutesBefore = reminderMins
                    )
                } else loc
            }
            state.copy(
                locations = updatedLocations,
                statusBannerMessage = "Automation & shift reminder rules saved."
            )
        }
    }
}
