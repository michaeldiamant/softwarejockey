package softwarejockey.validation


trait SymbolDao {
  
  def findByName(name: String): Option[Symbol]
}

trait MarketTakerDao {
  
  def findByExternalId(externalId: String): Option[MarketTaker]
}

trait MarketMakerDao {
  
  def findByExternalId(externalId: String): Option[MarketMaker]
}

