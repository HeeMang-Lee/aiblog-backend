package com.aiblog.domain.seo.controller;

import com.aiblog.domain.seo.service.SitemapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SeoController {

  private final SitemapService sitemapService;

  @GetMapping(value = "/sitemap.xml",
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> getSitemap() {
    return ResponseEntity.ok(sitemapService.generateSitemap());
  }

  @GetMapping(value = "/robots.txt",
      produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> getRobotsTxt() {
    return ResponseEntity.ok(sitemapService.generateRobotsTxt());
  }
}
