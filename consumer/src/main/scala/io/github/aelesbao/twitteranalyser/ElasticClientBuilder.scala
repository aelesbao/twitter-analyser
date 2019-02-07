package io.github.aelesbao.twitteranalyser

import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties, NoOpRequestConfigCallback}
import com.typesafe.scalalogging.LazyLogging
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback

object HttpClientCredentialsConfigCallback extends HttpClientConfigCallback {

  private val credentials = new UsernamePasswordCredentials(
    ConsumerConfig.elasticsearch.username,
    ConsumerConfig.elasticsearch.password
  )

  private val credentialsProvider: CredentialsProvider = new BasicCredentialsProvider {
    setCredentials(AuthScope.ANY, credentials)
  }

  override def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder =
    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
}

object ElasticClientBuilder extends LazyLogging {
  def build(): ElasticClient = {
    val properties = ElasticProperties(ConsumerConfig.elasticsearch.endpoint)
    ElasticClient(properties, NoOpRequestConfigCallback, HttpClientCredentialsConfigCallback)
  }
}
