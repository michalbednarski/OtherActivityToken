This is exploit for CVE-2025-0098 ([bulletin](https://source.android.com/docs/security/bulletin/2025-02-01#framework), [patch](https://android.googlesource.com/platform/frameworks/base/+/61ab2b65caf855c48fdb4166f94e02bf79c90e7b%5E!/#F0)), `Activity` token leak due to pid-only check (and then use it for arbitrary `startActivity()`)

This one comes without full writeup (mostly because I didn't write one because I didn't mean this report to be exploit chain (although it ultimately was recognized as one, but on the other hand [ThisSeemsWrong](https://github.com/michalbednarski/ThisSeemsWrong) wasn't when I hoped it to, so we're even))

However after short discussion with Canyie ([Blog](https://blog.canyie.top/), [GitHub](https://github.com/canyie)) and comparing their approach to having `ActivityRecord.pid == 0` (having process exit through [fail due to Intent exceeding one-way Binder buffer limit](https://blog.canyie.top/2026/06/09/android-security-tricks/#oneway-%E8%B0%83%E7%94%A8%E7%9A%84%E6%95%B0%E6%8D%AE%E5%A4%A7%E5%B0%8F%E9%99%90%E5%88%B6) vs my approach here of having `ActivityRecord` not (yet) attached), I've decided that it is still worth publishing

(Maybe I'll make full writeup if there's interest, maybe this one will stay as one for own analysis, [if you have questions you're welcome to ask in issues](https://github.com/michalbednarski/OtherActivityToken/issues))

For historical accuracy code in this report matches one that was originally sent to Google, if you'd like trying debugging it on modern AOSP (tested on `android-17.0.0_r1`), you'd need to revert fix

```bash
cd frameworks/base
git revert 61ab2b65caf855c48fdb4166f94e02bf79c90e7b
```

As well as apply following change to this exploit repository (looks like this is due to unrelated change within art, but it doesn't really matter as this is thing that usual [`setAccessible` reflection](https://developer.android.com/reference/java/lang/reflect/AccessibleObject#setAccessible(boolean)) jumps over, just using linking instead of reflection was more convenient for me)

```diff
diff --git a/app/src/main/java/com/example/otheractivitytoken/AppThreadRelay.java b/app/src/main/java/com/example/otheractivitytoken/AppThreadRelay.java
index 711e434..2ce0256 100644
--- a/app/src/main/java/com/example/otheractivitytoken/AppThreadRelay.java
+++ b/app/src/main/java/com/example/otheractivitytoken/AppThreadRelay.java
@@ -37,7 +37,7 @@ class AppThreadRelay extends Binder {
         boolean doRelay = mRelayedCodes.contains(code);
         Log.v("OtherToken", "FakeThread code=" + code + " doRelay=" + doRelay);
         if (doRelay) {
-            ActivityThread.currentActivityThread().getApplicationThread().onTransact(code, data, reply, flags);
+            ActivityThread.currentActivityThread().getApplicationThread().transact(code, data, reply, flags);
         }
         return true;
     }
```
