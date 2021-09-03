package io.logto.android.constant

class AuthConstant {
    class QueryKey {
        companion object {
            const val CLIENT_ID = "client_id"
            const val CODE = "code"
            const val CODE_CHALLENGE = "code_challenge"
            const val CODE_CHALLENGE_METHOD = "code_challenge_method"
            const val CODE_VERIFIER = "code_verifier"
            const val GRANT_TYPE = "grant_type"
            const val PROMPT = "prompt"
            const val REDIRECT_URI = "redirect_uri"
            const val RESOURCE = "resource"
            const val RESPONSE_TYPE = "response_type"
            const val SCOPE = "scope"
        }
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
