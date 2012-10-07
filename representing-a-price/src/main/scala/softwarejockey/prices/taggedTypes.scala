package softwarejockey.prices

/**
 * Credit to Miles Sabin's gist:  https://gist.github.com/89c9b47a91017973a35f
 */
object TaggedTypes {
  type Tagged[U] = {type Tag = U}
  type @@[T, U] = T with Tagged[U]
  // Thanks to @retronym for suggesting this type alias

  class Tagger[U] {
    def apply[T](t: T): T @@ U = t.asInstanceOf[T @@ U]
  }

  def tag[U] = new Tagger[U]
}
