const SAVED_ACCOUNTS_KEY = 'savedLoginAccounts'
const LAST_LOGIN_USERNAME_KEY = 'lastLoginUsername'
const LEGACY_SAVED_LOGIN_KEY = 'savedLoginCredentials'

export interface SavedLoginAccount {
  username: string
  password: string
}

function migrateLegacySavedLogin() {
  const legacy = localStorage.getItem(LEGACY_SAVED_LOGIN_KEY)
  if (!legacy) return
  try {
    const data = JSON.parse(legacy) as SavedLoginAccount
    if (data.username && data.password) {
      saveLoginAccount(data)
    }
  } catch {
    /* ignore invalid data */
  }
  localStorage.removeItem(LEGACY_SAVED_LOGIN_KEY)
}

export function loadSavedAccounts(): SavedLoginAccount[] {
  migrateLegacySavedLogin()
  const raw = localStorage.getItem(SAVED_ACCOUNTS_KEY)
  if (!raw) return []
  try {
    const data = JSON.parse(raw) as SavedLoginAccount[]
    return Array.isArray(data)
      ? data.filter(item => item.username && item.password)
      : []
  } catch {
    return []
  }
}

export function getAccountByUsername(username: string): SavedLoginAccount | null {
  const trimmed = username.trim()
  if (!trimmed) return null
  return loadSavedAccounts().find(item => item.username === trimmed) ?? null
}

export function getLastLoginAccount(): SavedLoginAccount | null {
  migrateLegacySavedLogin()
  const lastUsername = localStorage.getItem(LAST_LOGIN_USERNAME_KEY)
  if (!lastUsername) return null
  return getAccountByUsername(lastUsername)
}

export function saveLoginAccount(account: SavedLoginAccount) {
  const accounts = loadSavedAccounts().filter(item => item.username !== account.username)
  accounts.push({ username: account.username.trim(), password: account.password })
  localStorage.setItem(SAVED_ACCOUNTS_KEY, JSON.stringify(accounts))
  localStorage.setItem(LAST_LOGIN_USERNAME_KEY, account.username.trim())
}

export function removeLoginAccount(username: string) {
  const trimmed = username.trim()
  const accounts = loadSavedAccounts().filter(item => item.username !== trimmed)
  localStorage.setItem(SAVED_ACCOUNTS_KEY, JSON.stringify(accounts))
  if (localStorage.getItem(LAST_LOGIN_USERNAME_KEY) === trimmed) {
    localStorage.removeItem(LAST_LOGIN_USERNAME_KEY)
  }
}

export function preserveSavedLoginOnClear() {
  const accounts = localStorage.getItem(SAVED_ACCOUNTS_KEY)
  const lastUsername = localStorage.getItem(LAST_LOGIN_USERNAME_KEY)
  localStorage.clear()
  if (accounts) localStorage.setItem(SAVED_ACCOUNTS_KEY, accounts)
  if (lastUsername) localStorage.setItem(LAST_LOGIN_USERNAME_KEY, lastUsername)
}
