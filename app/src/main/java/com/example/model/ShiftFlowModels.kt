package com.example.model

enum class WorkspaceMode(val label: String) {
    EMPLOYEE_MOBILE("Employee App"),
    MANAGER_WEB("Manager Web Grid")
}

enum class EmployeeMobileTab(val label: String) {
    SCHEDULE("Schedule"),
    AVAILABILITY("Availability"),
    ALERTS("Alerts"),
    WORK_CHAT("WorkChat"),
    PROFILE("Profile")
}

enum class EmployeeScheduleSubTab(val label: String) {
    MY_SHIFTS("MY SHIFTS"),
    ALL_SHIFTS("ALL SHIFTS"),
    OPEN_SHIFTS("OPEN SHIFTS")
}

enum class ManagerWebSection(val label: String) {
    ROSTER("Roster"),
    AVAILABILITY("Availability"),
    COVERAGE("Call-outs & coverage"),
    TEMPLATES("Templates & sites"),
    EMPLOYEES_ROLES("Employees & roles"),
    REPORTS("Reports"),
    SETTINGS("Settings")
}

enum class WeekStartDay(val label: String, val orderedDays: List<String>, val shortDays: List<String>) {
    MONDAY(
        "Monday",
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
        listOf("Mon 16", "Tue 17", "Wed 18", "Thu 19", "Fri 20", "Sat 21", "Sun 22")
    ),
    SUNDAY(
        "Sunday",
        listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"),
        listOf("Sun 15", "Mon 16", "Tue 17", "Wed 18", "Thu 19", "Fri 20", "Sat 21")
    ),
    SATURDAY(
        "Saturday",
        listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"),
        listOf("Sat 14", "Sun 15", "Mon 16", "Tue 17", "Wed 18", "Thu 19", "Fri 20")
    )
}

data class StoreLocation(
    val id: String,
    val name: String,
    val shortName: String,
    val address: String,
    val jobSites: List<String>,
    val weekStartDay: WeekStartDay = WeekStartDay.MONDAY,
    val minorWeeklyHourLimit: Int = 16,
    val maxStandardWeeklyHours: Int = 40,
    val autoApproveQualifiedSwaps: Boolean = true,
    val autoApproveTimeOffUnder24h: Boolean = true,
    val shiftReminderMinutesBefore: Int = 60
)

data class CustomSkill(
    val id: String,
    val name: String,
    val category: String,
    val colorHex: Long,
    val bgHex: Long,
    val activeStaffCount: Int
)

enum class SystemRole(val label: String, val summary: String) {
    OWNER("Owner", "1 person · full access to everything"),
    MANAGER("Manager", "2 people · builds rosters, handles coverage"),
    ASSISTANT_MANAGER("Assistant Manager", "2 people · approves swaps, leads shifts"),
    EMPLOYEE("Employee", "12 people · submits availability, views own shifts")
}

enum class EmployeeStatus(val label: String) {
    ACTIVE("Active"),
    INVITED("Invited"),
    INACTIVE("Inactive")
}

data class Employee(
    val id: String,
    val shortName: String,
    val fullName: String,
    val initials: String,
    val avatarBgHex: Long,
    val role: SystemRole,
    val primaryPosition: String,
    val skills: List<String>,
    val contact: String,
    val status: EmployeeStatus,
    val scheduledHours: Int,
    val isMinor: Boolean = false,
    val age: Int? = null,
    val locationIds: List<String> = listOf("main_st", "columbia_csc")
)

enum class AvailabilitySlot(val label: String) {
    MORNING("Morning"),
    EVENING("Evening"),
    OFF("Off")
}

data class DayAvailability(
    val dayIndex: Int,
    val dayName: String,
    val dateLabel: String,
    val selectedSlot: AvailabilitySlot
)

enum class RequestStatus(val label: String) {
    APPROVED("Auto-Approved"),
    PENDING("Pending Review"),
    DECLINED("Declined")
}

data class TimeOffRequest(
    val id: String,
    val employeeId: String,
    val employeeName: String,
    val typeLabel: String, // "Time Off" or "Temporary Unavailability"
    val dateRange: String,
    val reason: String,
    val status: RequestStatus,
    val isRecurringUnavailability: Boolean = false
)

enum class ShiftStatus(val label: String) {
    PUBLISHED("Published"),
    DRAFT("Draft"),
    OPEN("Open Shift"),
    CALLED_OUT("Called Out Sick"),
    SWAP_REQUESTED("Swap Offered")
}

data class ShiftItem(
    val id: String,
    val locationId: String,
    val locationName: String,
    val jobSite: String,
    val employeeId: String?,
    val employeeShortName: String?,
    val employeeFullName: String?,
    val employeeInitials: String?,
    val avatarBgHex: Long,
    val dayIndex: Int, // 0 = Mon 16 .. 6 = Sun 22 (or negative for Nov 12-15)
    val dayHeader: String, // e.g. "Mon 16", "Sat, Nov 14"
    val dayAbbrev: String, // "MON", "TUE"
    val dateNum: String,   // "16", "17"
    val startTime: String, // "9:00 AM"
    val endTime: String,   // "2:00 PM"
    val compactTime: String, // "9 AM – 2 PM"
    val durationHours: Int,
    val shiftLabel: String, // "Opener", "Closer", "Lead", "Scooper"
    val skillRequired: String, // "Scooper", "Shift Lead", "Ice Cream Maker", "Cake Decorator", "Cashier"
    val status: ShiftStatus = ShiftStatus.PUBLISHED,
    val isConfirmed: Boolean = true,
    val hasNote: Boolean = false,
    val noteText: String = ""
)

data class ShiftTemplate(
    val id: String,
    val title: String,
    val timeRange: String,
    val startTime: String,
    val endTime: String,
    val compactTime: String,
    val durationHours: Int,
    val requiredSkill: String,
    val jobSite: String
)

data class ScheduleTemplate(
    val id: String,
    val name: String,
    val subtitle: String,
    val totalWeeklyHours: Int,
    val shiftsCount: Int,
    val tags: List<String>
)

enum class CandidateInviteStatus(val label: String) {
    ASKED("Asked"),
    DECLINED("Declined"),
    READY("Best Match"),
    ASSIGNED("Assigned")
}

data class CoverageCandidate(
    val employeeId: String,
    val rank: Int,
    val name: String,
    val initials: String,
    val weeklyHours: Int,
    val overlapSummary: String,
    val availabilityTag: String,
    val capabilityTag: String,
    val isPartialAvailability: Boolean = false,
    val status: CandidateInviteStatus = CandidateInviteStatus.READY
)

enum class CallOutReason(val label: String) {
    SICK("I'm sick"),
    FAMILY_EMERGENCY("Family emergency"),
    OTHER("Other")
}

data class AlertItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timeAgo: String,
    val isUrgentCover: Boolean = false,
    val urgentShiftId: String? = null,
    val urgentTimeRange: String? = null,
    val urgentMeta: String? = null,
    val hoursAfterAccept: String? = null,
    val askedBy: String? = null,
    val expiresInMin: Int? = null,
    val isResolved: Boolean = false,
    val resolutionLabel: String? = null
)

data class ChatMessage(
    val id: String,
    val locationId: String,
    val senderName: String,
    val senderRole: String,
    val initials: String,
    val dateDivider: String? = null,
    val timestamp: String,
    val text: String,
    val isManagerBroadcast: Boolean = false,
    val isShiftCoverRequest: Boolean = false
)

data class NotificationPrefItem(
    val id: String,
    val title: String,
    val description: String,
    val pushEnabled: Boolean,
    val emailEnabled: Boolean = false,
    val textEnabled: Boolean = false,
    val supportsEmail: Boolean = true,
    val supportsText: Boolean = false
)

data class RolePermissionItem(
    val label: String,
    val owner: Boolean,
    val manager: Boolean,
    val assistantManager: Boolean,
    val employee: Boolean
)
