###How to use this lib
- Include Spring Dependency Injection
- Use (if not already done) to start your application: AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
- Register this needed config: context.register(ConnectivityManager.class);
- Add to your executing class @ComponentScan(basePackages = "de.mixedfx.network")