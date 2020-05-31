sealed class Either<out L, out R> {
    data class Left<out L>(val left: L) : Either<L, Nothing>()
    data class Right<out R>(val right: R) : Either<Nothing, R>()

    val isRight get() = this is Right<R>
    val isLeft get() = this is Left<L>
    fun <A> fold(fnL: (L) -> A, fnR: (R) -> A): A =
        when (this) {
            is Left -> fnL(left)
            is Right -> fnR(right)
        }

    companion object {
        fun <L> left(a: L) = Either.Left(a)
        fun <R> right(b: R) = Either.Right(b)
    }
}