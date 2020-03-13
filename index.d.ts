export interface SmsListenerEvent {
  message?: string
  extras?: string
  status?: string
  timeout?: string
}

declare const SmsRetrieverModule: {
  requestPhoneNumber: () => Promise<string>
  startSmsRetriever: () => Promise<boolean>
  addSmsListener: (callback: (event: SmsListenerEvent) => void) => Promise<boolean>
  removeSmsListener: () => void,
  getAppSignature: () => Promise<string>
}

export default SmsRetrieverModule
