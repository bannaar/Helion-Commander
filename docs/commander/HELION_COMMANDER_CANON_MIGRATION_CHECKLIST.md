# HELION COMMANDER CANON MIGRATION CHECKLIST

## Player-facing corrections
- [x] Main navigation label GalNet -> UniNet.
- [x] Route labels Null-Sec -> Zero Space while retaining legacy internal enum identifiers temporarily.
- [x] Fake Aurelia seed restored to Aurelian Synod sovereignty/high security.
- [x] Fake Commonwealth capital display Concordia -> Crownspire.
- [x] Fake Titan Forge manufacturer display -> Ironstar Forge.
- [x] Prototype environment descriptions no longer claim a live connection.
- [x] INTERNET permission added for future RealCompanionApi.
- [ ] Continue replacing remaining player-facing CR/Credits with GSC as screens are touched.
- [ ] Continue replacing remaining player-facing GalNet strings with UniNet while preserving compatibility-sensitive internals.
- [ ] Review every mock ship/module/company name against DraftComp7 before promoting it to canon.

## Compatibility rule
Classify every stale occurrence:
1. UI/content string: update now.
2. Non-persisted internal symbol: rename when safe.
3. Persisted/schema/protocol identifier: use explicit tested migration.
4. Historical record: preserve as history.

Legacy internal names such as GalNetArticle, galnet routes, NULL_SECURITY, AVOID_NULLSEC, credits fields, fac-concordat, or sys-concordia do not become current lore merely because they remain in code.

## Data truth
For every README/UI feature label it mentally as VERIFIED SERVER, LOCAL/CACHED, MOCKED, PLANNED, or UNKNOWN.
FakeCompanionApi is MOCKED.
