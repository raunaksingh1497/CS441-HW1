

object Main {
  def main(args: Array[String]): Unit = {
    // Start MainService
    println("Starting MainService...")
    mainservice.MainService.main(Array.empty)

    // Start LLMServer
    println("Starting LLMServer...")
    llmserver.LLMServer.main(Array.empty)
  }
}