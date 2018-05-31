# HomeAwayPlaces
The app lets the user search for near by places around Seattle, Washington. App is built using jVanila framework. jVanila is a technology agnostic framework which works as a clean wrapper by abstracting each mobile platform complexities, this framewrok is developed by me.

The deliverable meets all the given requirements while showcasing a technology agnostic approach.

  - The solution is built by applying MVP architecture pattern, by doing so, the right logic sits at the right place
  - Infra and business layers are cleanly separated out for better maintenance and code reuse
  - In a nutshell, android-specific logic taken care by UI layer, presentation-logic in Presentation layer, business-logic is domain layer    - The code is developed in pure MVP way, it means more than 95% is dictated by Presenters / Data Binders, it means the UI layer is very dumb, the Presenters behaves as the smart components
  - It showcases a case study of how to do a multi-language compatible data binding 
  - Tried to get a professional quality UI though I’m not a designer ;)
  - The places api is very loose-coupled from vendor specific APIs Foursquare, GooglePlaces etc. The advantage of doing like this is there is no vendor lock in business logic. The impact of switching from one 3rd party API library to other is nothing. Current app supports both Foursqaure and Google Places API 
  - App provides a simplest way to change the Places API, by just calling a single line. PlacesApi.withProvider(...). That’s it as simple as that to switch between API providers
  - Though the Point Of Interest is Seattle, WA, The app is developed in such a way that, it can take any location as Place of Interest or user's location itself -- But due to time constraint, left it incomplete
  - I’m even submitting the iOS code as part of the same task, which is 50% completed, (https://github.com/pavan2you/HomeAwayPlacesIOS). You can just traverse the code, it doesn’t contain the UI layer. This is just a by-product of my jVanilla framework which I’m able to get it in just in 3-4hrs.
  - It clearly conveys if a developer approach is in-depth, scalable, developing same requirements for android/iOS is pretty same and can gain benefits of sharing code
  - You can find class level / logic level comments across the source code
  - Few notable technical challenges addressed are common resource sharing, common data binding, sharing native code, simple event bus, a template way of developing sync layer code (daos, dtos, gateways).
  - I’ve in past developed a custom tool, to generate entire sync layer, though I don’t have the tool now, I’m trying to preserve the same structure, you could notice sync layer classes are very identical except the data what they operate. 
  - There was an interesting R&D, done by me, which is related to Multi screen porting, I had shown a very simplistic approach in dimension files. Wherever proportional scaling is applicable this approach works like a magic.

I've started this exercise on 27th May 2018, 11:45 and spent 9hrs. Then after spent 4-5hrs daily. Roughly I've taken 25hrs for Android, 50% iOS and ~7500 line of code altogether.

Please refer the app flow below.

1. Very first experience

![image](https://user-images.githubusercontent.com/3917434/40803725-f3388b40-6536-11e8-8876-89d5b178824f.png) ![image](https://user-images.githubusercontent.com/3917434/40803847-445c1d52-6537-11e8-972f-28adc54248ce.png) 

2. Keyboard off, and with few favourites and Menu options

![image](https://user-images.githubusercontent.com/3917434/40803919-7b47bd3a-6537-11e8-8304-4e653ad8f719.png) ![image](https://user-images.githubusercontent.com/3917434/40804233-4aca0478-6538-11e8-8eb3-c38809caaf58.png)

3. Map full screen view and place detail view

![image](https://user-images.githubusercontent.com/3917434/40804379-b635b1f8-6538-11e8-992a-5951dcc53698.png) ![image](https://user-images.githubusercontent.com/3917434/40804073-ef401d90-6537-11e8-95ac-29e09615cc75.png)







