# 🔍 Debug 403 Forbidden Error

## Issue
Still getting 403 Forbidden on `/api/admin/metrics` even after logging in as admin.

---

## ✅ Step 1: Check Browser Console

Open **Browser Console** (F12) and run these commands:

```javascript
// Check if token exists
console.log('Token:', localStorage.getItem('token'))

// Check role
console.log('Role:', localStorage.getItem('role'))

// Check username
console.log('Username:', localStorage.getItem('username'))
```

**Expected Output:**
```
Token: eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4i... (long JWT string)
Role: ADMIN
Username: admin
```

**If Role is NOT "ADMIN":**
- You're logged in as wrong user
- Logout and login with `admin` / `admin123`

---

## ✅ Step 2: Check Network Tab

In the **Network tab**, click on the failed `metrics` request and check:

### Request Headers
Should have:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**If Authorization header is MISSING:**
- Token not being sent
- Axios interceptor not working

**If Authorization header EXISTS but still 403:**
- JWT token is invalid or expired
- Role in JWT doesn't match

---

## ✅ Step 3: Decode JWT Token (Optional)

Copy the token and paste it at https://jwt.io

Check the **Payload** section. Should see:
```json
{
  "sub": "admin",
  "role": "ADMIN",
  "iat": 1234567890,
  "exp": 1234567890
}
```

**If "role" is missing or wrong:**
- Backend JWT generation issue

---

## ✅ Step 4: Force Fresh Login

1. **Clear All Data:**
   ```javascript
   localStorage.clear()
   ```

2. **Refresh Page** (F5)

3. **Login Again:**
   - Username: `admin`
   - Password: `admin123`

4. **Check Console After Login:**
   ```javascript
   console.log('Token:', localStorage.getItem('token'))
   console.log('Role:', localStorage.getItem('role'))
   ```

5. **Try Accessing Dashboard Again**

---

## ✅ Step 5: Check Backend Logs

Look at your **backend console** for any errors like:

```
JWT signature does not match
Invalid token
Bad credentials
User not found
```

---

## 🔧 Quick Fix Commands

Run these in **Browser Console**:

```javascript
// 1. Check current auth state
console.log({
  token: localStorage.getItem('token')?.substring(0, 50) + '...',
  role: localStorage.getItem('role'),
  username: localStorage.getItem('username'),
  isAuthenticated: !!localStorage.getItem('token')
})

// 2. If role is wrong, logout and clear
localStorage.clear()
window.location.href = '/login'
```

---

## 🎯 Most Common Causes

### 1. **Wrong User Logged In**
- ✅ **Solution:** Logout, login with `admin` / `admin123`

### 2. **Token Not Being Sent**
- ✅ **Solution:** Check Network tab for Authorization header

### 3. **JWT Role Claim Wrong**
- ✅ **Solution:** Check JWT payload at jwt.io

### 4. **Backend Not Recognizing Role**
- ✅ **Solution:** Check backend console for errors

### 5. **CORS Issue**
- ✅ **Solution:** Check if CORS is blocking requests

---

## 📋 Checklist

Before reporting the issue, verify:

- [ ] Logged in as `admin` (not maker1 or checker1)
- [ ] Token exists in localStorage
- [ ] Role is "ADMIN" in localStorage
- [ ] Authorization header is present in Network tab
- [ ] Backend is running without errors
- [ ] Frontend is running on port 3000
- [ ] Backend is running on port 8080

---

## 🆘 If Still Not Working

Run this diagnostic in console:

```javascript
// Comprehensive diagnostic
const diagnostic = {
  token: localStorage.getItem('token') ? 'EXISTS' : 'MISSING',
  role: localStorage.getItem('role'),
  username: localStorage.getItem('username'),
  tokenLength: localStorage.getItem('token')?.length || 0,
  frontendUrl: window.location.href,
  apiBaseUrl: '/api'
}
console.table(diagnostic)
```

Copy the output and share it.

---

**Next Step:** Run the commands above and tell me what you see! 🔍

