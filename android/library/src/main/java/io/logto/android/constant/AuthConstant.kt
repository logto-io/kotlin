package io.logto.android.constant

class AuthConstant {
    companion object {
        const val KEY_CLIENT_ID = "client_id"
        const val KEY_CODE_CHALLENGE = "code_challenge"
        const val KEY_CODE_CHALLENGE_METHOD = "code_challenge_method"
        const val KEY_PROMPT = "prompt"
        const val KEY_REDIRECT_URI = "redirect_uri"
        const val KEY_RESPONSE_TYPE = "response_type"
        const val KEY_CODE = "code"
        const val KEY_SCOPE = "scope"
        const val KEY_RESOURCE = "resource"
    }

    class PromptValue {
        companion object {
            const val CONSENT = "consent"
        }
    }

    class GrantType {
        companion object {
            const val AUTHORIZATION_CODE = "authorization_code"
        }
    }

    class ResponseType {
        companion object {
            const val CODE = "code"
        }
    }

    class CodeChallengeMethod {
        companion object {
            const val S256 = "S256"
        }
    }

    class ScopeValue {
        companion object {
            const val OPEN_ID = "openid"
            const val OFFLINE_ACCESS = "offline_access"
        }
    }

    class ResourceValue {
        companion object {
            const val LOGTO_API = "https://api.logto.io"
        }
    }
}
