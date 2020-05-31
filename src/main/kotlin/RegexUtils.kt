fun Regex.allSegment(input: String): Sequence<Either<MatchResult, String>> = sequence {
    val m = this@allSegment.findAll(input)
    var remain = 0..input.lastIndex
    m.forEach {
        (remain.first until it.range.first).takeIf { !it.isEmpty() }?.let { yield(Either.right(input.substring(it))) }
        yield(Either.left(it))
        remain = (it.range.last + 1)..remain.last
    }
    remain.takeIf { !it.isEmpty() }?.let { yield(Either.right(input.substring(it))) }
}