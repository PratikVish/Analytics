package com.upstox.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.upstox.config.properties.TradeFilePathConfigurationProperties;
import com.upstox.model.BarChartResponse;
import com.upstox.model.TradeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class TradeProcessor {

    @Autowired
    private TradeFilePathConfigurationProperties properties;

    @Autowired
    SimpMessagingTemplate template;

    private int bar_num = 1;

    private PriorityBlockingQueue<TradeData> queue;

    private AtomicBoolean enabled = new AtomicBoolean(false);

    private Map<String, BarChartResponse> hpBarDataInfo = new HashMap<>();

    private int ctr = 0;

    private List<BarChartResponse> response = new ArrayList<>(16);

    @PostConstruct
    private void init() {
        Runnable readJsonData = () -> {
            try {

                JsonFactory jsonfactory = new JsonFactory();
                File source = ResourceUtils.getFile(properties.getTradeFilePath());

                int cap = getCapacityForQueue(source);
                JsonParser parser = jsonfactory.createParser(source);
                queue = new PriorityBlockingQueue<>(cap);
                TradeData objTrade = new TradeData();

                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    if (parser.getText() == null) {
                        break;
                    }
                    String val = parser.getText().toLowerCase();
                    switch (val) {
                        case "{": {
                            objTrade = new TradeData();
                            break;
                        }
                        case "sym": {
                            parser.nextToken();
                            objTrade.setSym(parser.getText());
                            break;
                        }
                        case "t": {
                            parser.nextToken();
                            objTrade.setT(parser.getText());
                            break;
                        }
                        case "p": {
                            parser.nextToken();
                            objTrade.setP(Double.parseDouble(parser.getText()));
                            break;
                        }
                        case "q": {
                            parser.nextToken();
                            objTrade.setQ(Double.parseDouble(parser.getText()));
                            break;
                        }
                        case "ts": {
                            parser.nextToken();
                            objTrade.setTs(parser.getText());
                            break;
                        }
                        case "side": {
                            parser.nextToken();
                            objTrade.setSide(parser.getText());
                            break;
                        }
                        case "ts2": {
                            parser.nextToken();
                            objTrade.setTs2(parser.getText());
                            break;
                        }
                        case "}": {
                            queue.add(objTrade);
                            objTrade = null;
                            break;
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(readJsonData);
    }

    public void constructBarChartData() {
        enabled.set(true);
        log.debug("Start constructing bar chart data and push at every 15 second interval");
    }

    private BarChartResponse enrichBarChartData(TradeData data, boolean isIntervalOver) throws CloneNotSupportedException {

        BarChartResponse response = hpBarDataInfo.get(data.getSym());
        if (response == null) {
            response = new BarChartResponse();

            response.setSymbol(data.getSym());
            response.setEvent("ohlc_notify");
            response.setVolume(data.getQ());
            response.setBar_num(String.valueOf(bar_num));
            response.setO(String.valueOf(data.getP()));  // first price for symbol within the interval
            response.setC(0.0);
            response.setH(data.getP());
            response.setL(data.getP());
            hpBarDataInfo.put(data.getSym(), response);
        } else {
            response = hpBarDataInfo.get(data.getSym());

            if (response.getL() > data.getP()) {  // lowest price for symbol within the interval
                response.setL(data.getP());
            }

            if (response.getH() < data.getP()) { // highest price for symbol within the interval
                response.setH(data.getP());
            }

            response.setBar_num(String.valueOf(bar_num));
            response.setVolume(response.getVolume() + data.getQ());
        }

        if (isIntervalOver) {
            response.setC(data.getP()); //catch last price for symbol within the interval
        }
        BarChartResponse r = (BarChartResponse) response.clone();
        log.debug(r.toString());

        return r;
    }

    @Scheduled(fixedDelay = 15000)
    private void sendBarChartResponseToUsers() throws InterruptedException, CloneNotSupportedException {
        if (enabled.get()) {
            while (!queue.isEmpty()) {
                Thread.sleep(1000);
                ctr++;
                response.add(enrichBarChartData(queue.take(), ctr == 15 ? true : false));
                if (ctr == 15) {
                    ctr = 0;
                    bar_num++;
                    hpBarDataInfo.clear();
                    template.convertAndSend("/topic/ohlc_notify", response);
                    response.clear();
                }
            }

            if (queue.isEmpty()) {
                ctr++;
                if (ctr == 15) {
                    ctr = 0;
                    if (response.size() > 0) { // send remaining bars
                        template.convertAndSend("/topic/ohlc_notify", response);
                        response.clear();
                    } else { // send empty bars
                        BarChartResponse res = new BarChartResponse();
                        bar_num++;
                        res.setBar_num(String.valueOf(bar_num));
                        template.convertAndSend("/topic/ohlc_notify", res);
                    }
                }
            }
        }
    }

    private int getCapacityForQueue(File f) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        int lines = 1;
        while (reader.readLine() != null) lines++;
        reader.close();

        log.debug("File has " + lines + " lines, using this count to initialize PriorityBlockingQueue( initial cap )");
        return lines;
    }
}
