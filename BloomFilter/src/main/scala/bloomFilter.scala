import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf
import org.apache.spark.util.sketch.BloomFilter

object bloomFilter {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("BloomFilter")
      .setMaster("local[*]")

    val spark = SparkSession.builder()
      .config(conf)
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    val docsPath = "C:\\Users\\yazee\\IdeaProjects\\BloomFilter\\DOC\\*.txt"

    val docsRDD = spark.sparkContext.wholeTextFiles(docsPath)
    val wordsRDD = docsRDD.flatMap { case (_, content) => content.split("\\W+") }

    val numElements = 10000
    val falsePositiveRate = 0.01
    val bloomFilter = BloomFilter.create(numElements, falsePositiveRate)
    wordsRDD.collect().foreach(word => bloomFilter.put(word))

    val wordsInRDD = wordsRDD.collect().toSet
    val falsePositives = wordsInRDD.count(word => bloomFilter.mightContain(word) && !wordsInRDD.contains(word))
    val falseNegatives = wordsInRDD.count(word => !bloomFilter.mightContain(word) && wordsInRDD.contains(word))
    val totalWords = wordsInRDD.size
    val errorRate = (falsePositives + falseNegatives).toDouble / totalWords

    println(s"False Positives is : $falsePositives")
    println(s"False Negatives is : $falseNegatives")
    println(s"Total Wordsis : $totalWords")
    println(f"Empirical Error Rate is : $errorRate%.4f")

    spark.stop()
  }
}
