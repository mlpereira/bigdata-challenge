import org.apache.spark.sql._
import java.io._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.WildcardFileFilter
import org.apache.log4j.{Level, Logger}


object Recommendation {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Recommendation")

    val sc = SparkContext.getOrCreate(conf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)

    while(true) {
      val urls = new File("../views-per-url/url-parquets/").list

      val first = urls.headOption
      if (first != None) {
        urls.foreach { url =>
          val urlDF = sqlContext.read.parquet("../views-per-url/url-parquets/" + url)
          var scoresList : List[(String,Float)] = List()
          urls.foreach { url2 =>
            if (url != url2) {
              println("DF: " + url)
              urlDF.show()
              val url2DF = sqlContext.read.parquet("../views-per-url/url-parquets/" + url2)
              println("DF: " + url2)
              url2DF.show()
              val union = urlDF.union(url2DF).distinct().persist()
              val intersect = urlDF.intersect(url2DF).persist()
              println("Union")
              union.show()
              println("Intersection")
              intersect.show()
              println("----------------")
              val score = intersect.count().toFloat / union.count()
              println("Score: " + score)
              scoresList = scoresList:+((url2, score))
            }
          }
          val scoreUrls = new File("./score-parquets/").list
          if (scoreUrls.contains(url)) {
            FileUtils.deleteDirectory(new File("./score-parquets/" + url))
          }
          val rdd = sc.parallelize(scoresList)
          val dataframe = rdd.toDF
          println("Scores " + url)
          dataframe.show
          dataframe.write.parquet("./score-parquets/" + url)

        }
      }
      Thread.sleep(10000)
    }
  }
}
