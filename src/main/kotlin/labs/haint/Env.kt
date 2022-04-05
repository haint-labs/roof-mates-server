package labs.haint

sealed class Env(val value: String) {
    object Production : Env("production")
    object Development : Env("development")
}