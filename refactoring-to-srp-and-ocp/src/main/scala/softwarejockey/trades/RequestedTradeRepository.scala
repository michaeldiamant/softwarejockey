package softwarejockey.trades

trait RequestedTradeRepository {

  def findById(id: String): Option[RequestedTrade]

}
