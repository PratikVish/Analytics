Simple Design Criteria : 
To achieve the business requirement, I have used below technologies:

1. Spring Boot 
2. Spring Web Socket ( Message Broker for sending response under that channel, user subscribed to it will be notify by scheduler) 
3. SockJS 
4. StompJS for STOMP protocol
5. Spring Scheduler ( runs at 15 sec interval and send data to the topic configured )
6. Lombok (plugin for avoiding the boiler plate code)
7. Slf4J for logging
8. json-simple for parsing JSON file

Methods:
1. TradeProcessor.java - init() - it will read the Trade json file path & will create a collection of it.
2. TradeProcessor.java - getCapacityforQueue(File f) - get the count of no of lines in the json file, and use it as initial capacity for queue
3. TradeProcessor.java - sendBarChartResponseToUsers() - polls data from queue, and send bar chart data at every 15 seconds to the topic
4. TradeProcessor.java - constructBarChartData() - triggered when a new User is added to the system, and initializes to start the bar constructing process
5. TradeProcessor.java - enrichBarChartData(TradeData data, boolean isIntervalOver) - enrich data with proper BarChart Response
5. UserController - addUser(@Payload UserSubscription subscription, SimpMessageHeaderAccessor headerAccessor) 
					- invoked when Stock Client sends a message to a topic as configured in WebSocketConfig


Worker Thread 
1. Used executor service's 'newSingleThreadExecutor' to read the data from JSON file
2. Scheduler Thread to poll data and send it to the Message Broker as configured
3. All users subscribed to message broker - '/topic/ohlc_notify' should receive the response, Stomp client subscribes the user
	If needed we can have a hashmap of users (i.e. sessionid, name) and can use @SendToUser in which we can specify (user, topic, data)
	As all subscribed users should see the data, so i have used a public message broker, so all subscribed users should see all the data uptill now published
	
	
Data Structures
1. Priority Blocking Queue - For implementing a concept of polling the data ( Producer, Consumer kind of)
	TradeProcessor.java - private PriorityBlockingQueue<TradeData> queue;
2. Hashmap - For calculating the Open, High, Low, Close for 15sec bar interval per symbol
	private Map<String, BarChartResponse> hpBarDataInfo = new HashMap<>();
3. ArrayList - enrich the items within 15sec interval with help of 'enrichBarChartData()' add to list
	private List<BarChartResponse> response = new ArrayList<>(16);
	
Additional
1. Cloneable : used for returning a copy of the enrichment done over bar chart response
2. Comparable : used for sorting the data by filed TS2 as per requirement
3. images folder : shows how the screen works
4. log file path : its located in the analytical-server/target/logs/
5. test cases not covered as mostly methods are kept private