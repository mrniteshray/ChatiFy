# Troubleshooting: Search Not Showing Users

## ✅ Updates Applied

I've made the following improvements to help debug and fix the search issue:

### 1. **Enhanced Search Text Watcher**
- Now properly resets state when search is cleared
- Better handling of empty queries

### 2. **Added Debug Logging**
- Repository now logs all database operations
- You can see in Logcat what's happening during search

### 3. **Improved Data Validation**
- Checks for empty names before filtering
- Better null handling

---

## 🔍 How to Debug

### Step 1: Check Logcat
Run the app and look for these debug messages:

```
DEBUG: Total users in database: X
DEBUG: Search query: 'name', Current user ID: abc123
DEBUG: User found - UID: xyz, Name: 'John', Email: john@test.com
DEBUG: User matched query: John
DEBUG: Total matching users: X
```

### Step 2: Verify Firebase Realtime Database

1. Open **Firebase Console**
2. Go to **Realtime Database**
3. Check if data exists under `/users/`

Expected structure:
```
users/
  ├── user1_uid/
  │   ├── uid: "user1_uid"
  │   ├── name: "Alice"
  │   ├── email: "alice@test.com"
  │   ├── isOnline: true
  │   ├── lastSeen: 1234567890
  │   └── friends: []
  └── user2_uid/
      └── ...
```

---

## 🐛 Common Issues & Solutions

### Issue 1: No Users in Database
**Symptom**: Logcat shows "Total users in database: 0"

**Solution**: 
- Sign up at least 2 users
- Check Firebase Console to verify data is saved
- Verify your Firebase rules allow reads

### Issue 2: Database Permission Denied
**Symptom**: Error message about permissions

**Solution**: Update Firebase Rules (Console → Realtime Database → Rules):
```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

### Issue 3: User Data Not Saved During Signup
**Symptom**: Signup succeeds but user not in database

**Check**: Look for these log messages during signup:
```
DEBUG: Saving user to database - UID: abc, Name: John, Email: john@test.com
DEBUG: User saved successfully to /users/abc
```

**Solution**: 
- Check Firebase Console for data
- Verify internet connection
- Check Firebase rules

### Issue 4: Search Query Not Matching
**Symptom**: Users exist but search returns 0 results

**Check Logcat for**:
- User names being read correctly
- Query comparison (case-insensitive)
- Current user ID being excluded

**Solution**: Try searching with:
- Full name
- Partial name (should work)
- Different capitalization (should work)

---

## 🧪 Step-by-Step Testing

### Test 1: Create Test Users

1. **Open app in Emulator 1**
   - Sign up as: 
     - Name: "Alice Test"
     - Email: "alice@test.com"
     - Password: "123456"

2. **Open app in Emulator 2 (or logout from first)**
   - Sign up as:
     - Name: "Bob Test"  
     - Email: "bob@test.com"
     - Password: "123456"

3. **Check Firebase Console**
   - Go to Realtime Database
   - You should see both users under `/users/`

### Test 2: Search for Users

1. **Login as Alice** (alice@test.com)
2. Click the **(+)** icon to add friend
3. Type "Bob" in search box
4. **Check Logcat** for:
   ```
   DEBUG: Total users in database: 2
   DEBUG: Search query: 'Bob'
   DEBUG: User found - Name: 'Bob Test'
   DEBUG: User matched query: Bob Test
   DEBUG: Total matching users: 1
   ```

### Test 3: Verify Results Shown

If Logcat shows users found but UI doesn't show them:
- Check `SearchUsersState` in logs
- Verify RecyclerView adapter is set correctly
- Check if dialog is properly inflated

---

## 🔧 Quick Fixes

### Fix 1: Enable Firebase Database Persistence (Optional)
Add to `MainActivity.onCreate()`:
```kotlin
FirebaseDatabase.getInstance().setPersistenceEnabled(true)
```

### Fix 2: Verify Firebase Initialization
Check `google-services.json` is in `app/` folder

### Fix 3: Clear App Data
Sometimes cached data causes issues:
1. Settings → Apps → Chatify
2. Storage → Clear Data
3. Reinstall app

---

## 📊 What to Check in Firebase Console

### Database Tab:
```
✅ Check: /users/ node exists
✅ Check: Each user has all required fields
✅ Check: Names are spelled correctly
✅ Check: UIDs match Firebase Auth users
```

### Authentication Tab:
```
✅ Check: Users are created
✅ Check: Email verification not required (for testing)
```

### Rules Tab:
```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

---

## 🎯 Expected Behavior

1. **Type in search**: Should search in real-time
2. **Show loading**: Progress bar appears
3. **Show results**: Users matching query appear in list
4. **Click Add**: Friend is added to both users' lists
5. **Empty search**: Shows "Search for users by name"

---

## 📞 If Still Not Working

### Collect This Information:

1. **Logcat Output** (filter by "DEBUG"):
   - Copy all DEBUG logs when searching

2. **Firebase Database Screenshot**:
   - Show the `/users/` node structure

3. **Firebase Rules**:
   - Copy your current rules

4. **Error Messages**:
   - Any red errors in Logcat

### Then Check:
- [ ] Are you logged in? (Check `FirebaseAuth.getInstance().currentUser`)
- [ ] Is internet connected?
- [ ] Are Firebase services initialized?
- [ ] Is the search query actually triggering? (Check TextWatcher logs)

---

## 🚀 Testing Right Now

**Try this immediately:**

1. **Open Logcat** in Android Studio
2. **Filter by**: "DEBUG"
3. **Run the app**
4. **During signup**, look for: `DEBUG: Saving user...`
5. **During search**, look for: `DEBUG: Total users in database...`

The logs will tell you exactly where the issue is!

---

**If you see "Total users in database: 0"** → Users aren't being saved  
**If you see "Total matching users: 0"** → Search filter issue  
**If you see correct count but no UI** → UI state management issue  

The debug logs will pinpoint the exact problem! 🎯
