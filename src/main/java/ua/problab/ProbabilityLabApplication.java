/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * «Лабораторія ймовірностей» — навчальний веб-інструмент для уроків теорії ймовірностей.
 * <p>
 * Сервер (Spring Boot) реалізує ймовірнісне ядро: точні обчислення ймовірностей
 * у раціональних дробах, моделювання випадкових експериментів методом Монте-Карло,
 * генерацію та перевірку завдань. Веб-інтерфейс (HTML/CSS/JavaScript) віддається
 * з каталогу {@code static} і працює в будь-якому сучасному браузері.
 * <p>
 * Програма не використовує базу даних і не збирає персональних даних учнів.
 *
 * @author Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 *         А4.04 Середня освіта (Математика), НУЧК імені Т. Г. Шевченка
 */
@SpringBootApplication
public class ProbabilityLabApplication {

    private static final Logger log = LoggerFactory.getLogger(ProbabilityLabApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ProbabilityLabApplication.class, args);
    }

    /** Після старту друкує адреси, за якими вчитель і учні можуть відкрити програму. */
    @EventListener
    public void onServerStarted(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        log.info("Лабораторія ймовірностей. Автор: {}", Author.signature());
        log.info("Програма запущена. Відкрийте у браузері: http://localhost:{}", port);
        List<String> lan = localAddresses();
        if (!lan.isEmpty()) {
            for (String ip : lan) {
                log.info("Адреса для учнів у шкільній мережі: http://{}:{}", ip, port);
            }
        }
    }

    private static List<String> localAddresses() {
        List<String> result = new ArrayList<>();
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        result.add(addr.getHostAddress());
                    }
                }
            }
        } catch (Exception ignored) {
            // мережеві адреси — лише підказка для вчителя, помилка тут не критична
        }
        return result;
    }
}
