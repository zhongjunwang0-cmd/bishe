function pickEnglishVoice(): SpeechSynthesisVoice | undefined {
  const voices = window.speechSynthesis.getVoices()
  return (
    voices.find((v) => v.lang === 'en-US') ||
    voices.find((v) => v.lang.startsWith('en-US')) ||
    voices.find((v) => v.lang.startsWith('en'))
  )
}

export function isSpeechSupported(): boolean {
  return typeof window !== 'undefined' && 'speechSynthesis' in window
}

/** 使用浏览器 Web Speech API 朗读英文单词 */
export function speakEnglish(text: string): boolean {
  const word = text?.trim()
  if (!word || !isSpeechSupported()) return false

  window.speechSynthesis.cancel()

  const utterance = new SpeechSynthesisUtterance(word)
  utterance.lang = 'en-US'
  utterance.rate = 0.9

  const voice = pickEnglishVoice()
  if (voice) utterance.voice = voice

  window.speechSynthesis.speak(utterance)
  return true
}

/** 预加载语音列表（部分浏览器需异步加载 voices） */
export function preloadSpeechVoices(): void {
  if (!isSpeechSupported()) return
  window.speechSynthesis.getVoices()
  window.speechSynthesis.onvoiceschanged = () => {
    window.speechSynthesis.getVoices()
  }
}
