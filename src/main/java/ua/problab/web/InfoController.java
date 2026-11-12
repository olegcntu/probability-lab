/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.problab.Author;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Службова інформація для вчителя: за якими адресами учні можуть відкрити програму. */
@RestController
@RequestMapping("/api")
public class InfoController {

    public record Info(String name, String version, String author, String course, String group, String specialty,
                       String university, String year, List<String> lanUrls) {
    }

    @GetMapping("/info")
    public Info info(HttpServletRequest request) {
        int port = request.getLocalPort();
        List<String> urls = new ArrayList<>();
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        urls.add("http://" + addr.getHostAddress() + ":" + port + "/");
                    }
                }
            }
        } catch (Exception ignored) {
            // адреси — лише підказка
        }
        return new Info("Лабораторія ймовірностей", "1.0.0", Author.FULL_NAME, Author.COURSE, Author.GROUP,
                Author.SPECIALTY, Author.UNIVERSITY, Author.YEAR, urls);
    }
}
