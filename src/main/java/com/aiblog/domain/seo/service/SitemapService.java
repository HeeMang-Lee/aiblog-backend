package com.aiblog.domain.seo.service;

import com.aiblog.domain.post.entity.Post;
import com.aiblog.domain.post.entity.PostStatus;
import com.aiblog.domain.post.repository.PostRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SitemapService {

  private static final DateTimeFormatter W3C_DATE =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final PostRepository postRepository;
  private final String baseUrl;

  public SitemapService(
      PostRepository postRepository,
      @Value("${app.site.base-url}") String baseUrl) {
    this.postRepository = postRepository;
    this.baseUrl = baseUrl;
  }

  public String generateSitemap() {
    List<Post> posts = postRepository.findByStatusWithCategory(
        PostStatus.PUBLISHED);

    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

    for (Post post : posts) {
      xml.append("  <url>\n");
      xml.append("    <loc>")
          .append(escapeXml(baseUrl + "/posts/" + post.getSlug()))
          .append("</loc>\n");
      xml.append("    <lastmod>")
          .append(post.getUpdatedAt().format(W3C_DATE))
          .append("</lastmod>\n");
      xml.append("    <changefreq>weekly</changefreq>\n");
      xml.append("  </url>\n");
    }

    xml.append("</urlset>");
    return xml.toString();
  }

  public String generateRobotsTxt() {
    return "User-agent: *\n"
        + "Allow: /\n"
        + "Sitemap: " + baseUrl + "/sitemap.xml\n";
  }

  private String escapeXml(String value) {
    return value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("'", "&apos;")
        .replace("\"", "&quot;");
  }
}
