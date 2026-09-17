package com.vjagarwal.veyra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {
    @Value("${veyra.ai-provider:none}") String ai;
    @Value("${veyra.train-base-url:}") String train;
    @GetMapping("/status") public Object status() { return java.util.Map.of("aiProvider", ai, "trainConfigured", train != null && !train.isBlank(), "cloudOptional", true); }
}
