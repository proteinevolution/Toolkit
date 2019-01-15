import de.proteinevolution.migrations.services.MongobeeRunner

class ToolkitAppLoader extends GuiceApplicationLoader {

  private val materializerOverrides: Seq[GuiceableModule] = Seq(
    bind[Materializer].toProvider[MaterializerProvider]
  )

  private val config = ConfigFactory.load()

  private val mongoUri = config.getString("mongodb.uri")

  protected override def overrides(context: ApplicationLoader.Context): Seq[GuiceableModule] = {
    GuiceApplicationLoader.defaultOverrides(context) ++ materializerOverrides
  }

  override def builder(context: Context): GuiceApplicationBuilder = {
    MongobeeRunner.run(mongoUri)
    super.builder(context)
  }

}
