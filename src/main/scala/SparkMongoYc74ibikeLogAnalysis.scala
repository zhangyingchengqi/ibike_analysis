import com.mongodb.spark.MongoSpark
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * 离线日志分析
 * 1。 spark sql 方案
 */
object SparkMongoYc74ibikeLogAnalysis {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR) //配置日志
    val session = SparkSession.builder()
      .master("local")
      .appName("MongoSparkConnectorIntro")
      .config("spark.mongodb.input.uri", "mongodb://a:a@192.168.0.200:27017/yc74ibike.logs")
      .config("spark.mongodb.output.uri", "mongodb://a:a@192.168.0.200:27017/yc74ibike.result")  // result是用于存分析结果的集合
      .getOrCreate()
    val df: DataFrame = MongoSpark.load(session)
    //2 创建视图
    df.createTempView("v_logs")

    val pv = session.sql("select count(_id) from v_logs")
    println("page view:")
    pv.show()

    println( "将pv和uv一次性计算出来:")
    val uv = session.sql("select count(_id) pv, count(distinct openid) uv from v_logs")
    uv.show()

    MongoSpark.save(uv)   //保存到 mongo 的result中

    session.stop()
  }
}
