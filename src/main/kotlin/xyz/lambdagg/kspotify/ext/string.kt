package xyz.lambdagg.kspotify.ext

/**
 * https://stackoverflow.com/a/46022277
 */
fun String.insertAt(_position: Int, insert: String): String {
    val position = if (_position < 0) this.length + _position else _position

    require(position < this.length) { "position=$position" }

    if (insert.isEmpty()) {
        return this
    }

    if (position == 0) {
        return insert + this
    } else if (position == this.length) {
        return this + insert
    }

    val buffer = CharArray(this.length + insert.length)
    this.toCharArray(buffer, 0, 0, position)
    insert.toCharArray(buffer, position, 0, insert.length)
    this.toCharArray(buffer, position + insert.length, position, this.length)
    return String(buffer)
}
