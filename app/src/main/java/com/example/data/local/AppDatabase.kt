package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.DocumentTemplateDao
import com.example.data.local.entity.DocumentTemplateEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DocumentTemplateEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun documentTemplateDao(): DocumentTemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jurisdraft_local.db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    getInstance(context).documentTemplateDao().insertAll(getPrepopulatedTemplates())
                }
            }
        }

        fun getPrepopulatedTemplates(): List<DocumentTemplateEntity> {
            return listOf(
                DocumentTemplateEntity(
                    id = 1,
                    title = "Mutual Non-Disclosure Agreement (NDA)",
                    docType = "NDA",
                    jurisdiction = "California & Delaware",
                    description = "Bilateral trade secret and confidential information protection compliant with Cal. Bus. & Prof. Code § 16600.",
                    content = """
MUTUAL NON-DISCLOSURE AND CONFIDENTIALITY AGREEMENT

This Mutual Non-Disclosure Agreement ("Agreement") is entered into as of [EFFECTIVE DATE], by and between:

DISCLOSING / RECEIVING PARTY A: [FULL LEGAL ENTITY NAME A], a [STATE/JURISDICTION] corporation, with its principal place of business at [ADDRESS A] ("Party A"); and
DISCLOSING / RECEIVING PARTY B: [FULL LEGAL ENTITY NAME B], a [STATE/JURISDICTION] corporation, with its principal place of business at [ADDRESS B] ("Party B").

1. PURPOSE
The parties wish to explore a potential commercial or corporate transaction concerning [DESCRIBE SPECIFIC PURPOSE OR MATTER] (the "Purpose").

2. CONFIDENTIAL INFORMATION
"Confidential Information" means all non-public, proprietary information disclosed by either party ("Disclosing Party") to the other ("Receiving Party"), whether orally, visually, or in writing, designated as confidential or which reasonably should be understood as confidential given the nature of the information.
Confidential Information expressly excludes information that:
(a) is or becomes publicly known through no breach of this Agreement;
(b) was already in the rightful possession of Receiving Party prior to disclosure;
(c) is independently developed without reference to Disclosing Party's information.

3. OBLIGATIONS & STANDARD OF CARE
Receiving Party shall:
(a) hold all Confidential Information in strict confidence, exercising at least the same degree of care as it uses for its own confidential assets of like sensitivity, but in no event less than a reasonable standard;
(b) limit disclosure solely to its officers, directors, and legal advisors who have a need to know for the Purpose and who are bound by written confidentiality obligations at least as restrictive as this Agreement;
(c) not use Confidential Information for any reason other than the Purpose.

4. COMPLIANCE & RESTRICTIVE COVENANTS
Pursuant to governing jurisdiction laws, nothing in this Agreement shall be construed to restrain any employee or contractor from engaging in a lawful profession, trade, or business. Protection is strictly confined to proprietary trade secrets and confidential business materials.

5. TERM AND RETURN OF MATERIALS
The confidentiality obligations under this Agreement shall survive for a period of three (3) years from disclosure; provided that trade secrets shall remain protected for so long as they qualify as trade secrets under applicable law. Upon written demand, Receiving Party shall promptly return or certify destruction of all materials.

6. GOVERNING LAW AND DISPUTE RESOLUTION
This Agreement shall be governed by and construed in accordance with the laws of [GOVERNING JURISDICTION], without regard to conflict of laws principles.

IN WITNESS WHEREOF, the parties have executed this Agreement as of the date first above written.

PARTY A: ___________________________        PARTY B: ___________________________
By: [NAME AND TITLE]                        By: [NAME AND TITLE]
                    """.trimIndent(),
                    isBuiltIn = true
                ),
                DocumentTemplateEntity(
                    id = 2,
                    title = "Independent Contractor Services Agreement",
                    docType = "CONTRACT",
                    jurisdiction = "Delaware & US Commercial",
                    description = "Professional service agreement with clear IP assignment, work-for-hire provisions, and independent contractor classification.",
                    content = """
INDEPENDENT CONTRACTOR SERVICES AGREEMENT

This Independent Contractor Agreement ("Agreement") is made effective as of [EFFECTIVE DATE], between:
CLIENT: [COMPANY LEGAL NAME], located at [CLIENT ADDRESS] ("Client"), and
CONTRACTOR: [CONTRACTOR LEGAL NAME], located at [CONTRACTOR ADDRESS] ("Contractor").

RECITALS
Client desires to retain Contractor to perform specialized professional services, and Contractor represents that Contractor possesses the expertise and resources to perform such services on an independent basis.

1. SERVICES AND DELIVERABLES
Contractor shall render the legal, technical, or advisory services specified in Exhibit A ("Statement of Work"). Time is of the essence in the delivery of all agreed milestones.

2. INDEPENDENT CONTRACTOR RELATIONSHIP
Contractor is an independent contractor, not an employee, agent, or partner of Client. Contractor retains complete autonomy over the method, manner, and schedule of performing the services. Contractor is solely responsible for all federal, state, and local taxes, insurance, and withholdings.

3. COMPENSATION AND PAYMENT TERMS
Client shall pay Contractor the sum of $[FEE AMOUNT] per [HOUR / MILESTONE / PROJECT], payable within thirty (30) days of receipt of an itemized invoice.

4. INTELLECTUAL PROPERTY & WORK MADE FOR HIRE
All deliverables, work product, documentation, and inventions conceived or produced by Contractor in performing services hereunder shall constitute a "work made for hire" owned exclusively by Client. To the extent any deliverables do not qualify as work made for hire, Contractor hereby irrevocably assigns all right, title, and interest therein to Client.

5. INDEMNIFICATION AND LIMITATION OF LIABILITY
Contractor agrees to indemnify and hold harmless Client against any claims arising from gross negligence or willful misconduct in performance. Neither party shall be liable for consequential, incidental, or punitive damages.

6. GOVERNING LAW & JURISDICTION
This Agreement shall be governed by Delaware law. Any suit or proceeding arising hereunder shall be brought in the state or federal courts located in Wilmington, Delaware.

CLIENT: ___________________________        CONTRACTOR: ___________________________
Title: [AUTHORIZED SIGNATORY]               Title: [CONTRACTOR / PRINCIPAL]
                    """.trimIndent(),
                    isBuiltIn = true
                ),
                DocumentTemplateEntity(
                    id = 3,
                    title = "Notice of Motion and Motion for Extension of Time",
                    docType = "MOTION",
                    jurisdiction = "Federal District Court",
                    description = "Formal civil pleading seeking enlargement of discovery or pleading deadlines under Fed. R. Civ. P. 6(b).",
                    content = """
UNITED STATES DISTRICT COURT
[DISTRICT NAME, E.G., NORTHERN DISTRICT OF CALIFORNIA]

[PLAINTIFF NAME],
    Plaintiff,
v.                                              Case No. [CASE NUMBER]
[DEFENDANT NAME],
    Defendant.
____________________________________________/

NOTICE OF MOTION AND MOTION FOR EXTENSION OF TIME TO RESPOND TO COMPLAINT
[FED. R. CIV. P. 6(b)(1)(A)]

TO ALL PARTIES AND THEIR ATTORNEYS OF RECORD:
PLEASE TAKE NOTICE that [MOVING PARTY NAME] hereby moves this Honorable Court pursuant to Federal Rule of Civil Procedure 6(b)(1)(A) and Local Rule [LOCAL RULE NUMBER] for an Order extending the time for [MOVING PARTY] to answer, move, or otherwise respond to the Complaint up to and including [REQUESTED DATE].

MEMORANDUM OF POINTS AND AUTHORITIES

I. PROCEDURAL BACKGROUND
1. Plaintiff filed the original Complaint on [FILING DATE].
2. Moving Party was purportedly served on [SERVICE DATE]. The current deadline to respond is [CURRENT DEADLINE].
3. This is Moving Party's [FIRST / SECOND] request for an extension of time.

II. GOOD CAUSE EXISTS UNDER FED. R. CIV. P. 6(b)(1)(A)
Under Federal Rule of Civil Procedure 6(b)(1)(A), the Court may, for good cause, extend the time with or without motion if the request is made before the original time or its extension expires.
Good cause exists here because:
[STATE CONCISE FACTUAL GROUNDS: E.G., COUNSEL WAS RECENTLY RETAINED, ONGOING SETTLEMENT DISCUSSIONS, COMPLEXITY OF FACTUAL INQUIRY].

III. MEET AND CONFER CERTIFICATION
Pursuant to Civil Local Rules, Moving Counsel conferred with opposing counsel on [DATE]. Opposing counsel [DOES NOT OPPOSE / HAS BEEN CONTACTED].

CONCLUSION
WHEREFORE, Moving Party respectfully requests that this Court grant this Motion and extend the response deadline to [REQUESTED DATE].

Dated: [CURRENT DATE]
Respectfully submitted,

By: /s/ [ATTORNEY NAME]
State Bar No. [BAR NUMBER]
Attorney for [MOVING PARTY]
                    """.trimIndent(),
                    isBuiltIn = true
                ),
                DocumentTemplateEntity(
                    id = 4,
                    title = "Cease and Desist Demand (Intellectual Property / Brand Infringement)",
                    docType = "BRIEF",
                    jurisdiction = "United States Law / Lanham Act",
                    description = "Formal legal demand letter notifying infringing party of trademark or copyright violation and demanding cure within 10 days.",
                    content = """
VIA CERTIFIED MAIL AND ELECTRONIC TRANSMISSION
DATE: [DATE]

TO: [ALLEGED INFRINGING PARTY / ENTITY NAME]
[STREET ADDRESS]
[CITY, STATE, ZIP]
EMAIL: [CONTACT EMAIL]

RE: FORMAL DEMAND TO CEASE AND DESIST INFRINGEMENT OF [MARK / COPYRIGHT REGISTRATION NUMBER]

Dear [PARTY NAME OR COUNSEL]:

We represent [CLIENT / TRADEMARK OWNER NAME] ("Client") in matters relating to intellectual property enforcement. 

1. CLIENT'S EXCLUSIVE RIGHTS
Client is the registered owner of United States Trademark Registration No. [REGISTRATION NO.] for the mark "[TRADEMARK NAME]" in International Classes [CLASS NUMBERS], filed on [DATE] with the United States Patent and Trademark Office. Client has continuously used this mark in interstate commerce since [FIRST USE DATE], establishing substantial goodwill and commercial recognition.

2. INFRINGING CONDUCT
It has come to our attention that your organization is using the mark "[INFRINGING MARK OR PHRASE]" in connection with [OFFERING SERVICES / GOODS], marketed on [WEBSITE URL OR SOCIAL HANDLES]. Such use constitutes trademark infringement, false designation of origin under 15 U.S.C. § 1125(a) (Lanham Act § 43(a)), and unfair competition under state law.

3. LEGAL DEMANDS
To avoid formal legal action and statutory damages, we hereby demand that you immediately:
1. Cease and desist all marketing, distribution, and display of any materials bearing the mark;
2. De-index or take down infringing web domains and social accounts;
3. Provide written confirmation of compliance to our office by no later than 5:00 PM on [DEADLINE DATE - 10 DAYS].

Failure to comply will leave our client with no choice but to pursue all available judicial remedies, including injunctive relief, disgorgement of profits, statutory damages, and reasonable attorney's fees.

Sincerely,

[ATTORNEY NAME], Esq.
Counsel for [CLIENT NAME]
                    """.trimIndent(),
                    isBuiltIn = true
                ),
                DocumentTemplateEntity(
                    id = 5,
                    title = "Software as a Service (SaaS) Master Subscription Agreement",
                    docType = "CONTRACT",
                    jurisdiction = "New York / US Commercial",
                    description = "Enterprise Cloud Software terms including SLA, Data Protection (SOC2/GDPR), subscription tiers, and audit rights.",
                    content = """
MASTER SOFTWARE AS A SERVICE (SaaS) AGREEMENT

This Master Software as a Service Agreement ("Agreement") is made effective as of [EFFECTIVE DATE], between:
PROVIDER: [PROVIDER LEGAL NAME], a Delaware corporation ("Provider"), and
SUBSCRIBER: [CUSTOMER LEGAL NAME], a [STATE] entity ("Subscriber").

1. SUBSCRIPTION GRANT & SCOPE OF ACCESS
Subject to compliance with this Agreement and payment of applicable Fees, Provider grants Subscriber a non-exclusive, non-transferable right to access and use the SaaS Platform during the Subscription Term solely for Subscriber's internal business operations.

2. SERVICE LEVEL COMMITMENT (SLA) & SECURITY
Provider covenants an annual service uptime of 99.9% excluding scheduled maintenance. Provider maintains industry-standard technical and organizational safeguards (SOC 2 Type II compliant) to protect Customer Data against unauthorized disclosure.

3. FEES AND PAYMENT
Fees are invoiced annually in advance. Invoices are due net thirty (30) days. Past due balances shall accrue statutory interest at the rate of 1.5% per month or the legal maximum.

4. DATA OWNERSHIP AND PRIVACY
Subscriber retains all right, title, and interest in and to all data uploaded to the Platform ("Customer Data"). Provider shall process Customer Data only in accordance with Subscriber's instructions and applicable privacy laws (CCPA/GDPR).

5. MUTUAL LIMITATION OF LIABILITY
EXCEPT FOR INDEMNIFICATION OBLIGATIONS AND WILLFUL MISCONDUCT, NEITHER PARTY'S AGGREGATE LIABILITY ARISING OUT OF THIS AGREEMENT SHALL EXCEED THE TOTAL FEES PAID OR PAYABLE BY SUBSCRIBER IN THE TWELVE (12) MONTHS PRECEDING THE CLAIM.

6. GOVERNING LAW AND VENUE
This Agreement shall be governed by the laws of the State of New York. The parties consent to exclusive jurisdiction in New York County, New York.

PROVIDER: ___________________________       SUBSCRIBER: ___________________________
By: [NAME & TITLE]                          By: [NAME & TITLE]
                    """.trimIndent(),
                    isBuiltIn = true
                )
            )
        }
    }
}
