const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- SECURITY BOUNDS TESTS ---

test("Unauthenticated user: cannot read documents", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("documents").get());
});

test("Authenticated user: cannot read another lawyer's documents", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("documents").doc("doc_bob").set({
      userId: BOB_UID,
      title: "Bob's NDA",
      docType: "NDA",
      jurisdiction: "Delaware",
      content: "Confidentiality terms...",
      status: "DRAFT",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("documents").doc("doc_bob").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("documents").get());
});

test("Authenticated user: can create a valid legal document for themselves", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("documents").doc("doc_1").set({
      userId: ALICE_UID,
      title: "Motion for Summary Judgment",
      docType: "MOTION",
      jurisdiction: "US Federal",
      matterRef: "MATTER-2026-01",
      content: "Plaintiff hereby moves for summary judgment pursuant to Fed. R. Civ. P. 56...",
      status: "DRAFT",
      complianceScore: 92,
      lastAuditSummary: "Meets Celotex burden standard",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: rejected if creating document with another user's ID", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("documents").doc("doc_hack").set({
      userId: BOB_UID,
      title: "Hacked doc",
      docType: "CONTRACT",
      jurisdiction: "California",
      content: "Invalid",
      status: "DRAFT",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: rejected if docType is invalid enum", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("documents").doc("doc_invalid_enum").set({
      userId: ALICE_UID,
      title: "Invalid Type Doc",
      docType: "NOT_A_REAL_TYPE",
      jurisdiction: "California",
      content: "Sample text",
      status: "DRAFT",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can create and read audit records under their own document", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  // First seed or create parent doc
  await aliceDb.collection("users").doc(ALICE_UID).collection("documents").doc("doc_1").set({
    userId: ALICE_UID,
    title: "Employment Agreement",
    docType: "CONTRACT",
    jurisdiction: "California",
    content: "Non-compete clause...",
    status: "UNDER_REVIEW",
    createdAt: new Date(),
    updatedAt: new Date(),
  });

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("documents").doc("doc_1").collection("audits").doc("audit_1").set({
      userId: ALICE_UID,
      documentId: "doc_1",
      complianceScore: 65,
      overallAssessment: "California Bus. & Prof. Code § 16600 voids non-compete clauses.",
      riskLevel: "HIGH",
      issues: ["Non-compete clause is unenforceable in California under Edwards v. Arthur Andersen LLP"],
      citations: ["Edwards v. Arthur Andersen LLP, 44 Cal. 4th 937 (2008)"],
      recommendations: ["Replace non-compete with standard non-solicitation of trade secrets"],
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("documents").doc("doc_1").collection("audits").doc("audit_1").get()
  );
});
