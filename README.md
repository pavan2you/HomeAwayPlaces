# HomeAwayPlaces
The app searches the near by places around Seattle, Washington using jvanila framework. jVanila is a technology agnostic framework
which works as a clean wrapper by abstracting each mobile platform complexities, that is developed by me.

The task has addressed all the expected requirements and not just limited there, it also showcases how to develop by taking a technolgy
agnostic approach. 

  - It follows MVP architecture pattern, so that clearly seprating business logic from infra.
  - It applies Multi-langauge compatible data binding.
  - The code is developed in pure MVP way, it means > 95% is dictated by Presenters / Data Binders.
  - UI quality tried to keep it as professional as possible, though Im not a UI/UI guy ;).
  
  - The places api is very loose-coupled to vendor specific apis Foursquare, GooglePlaces etc. The advantage of this is, the impact of
  switching from one library to other is nothing. App provides a simplest way to change the same, by just calling one line.
  PlacesApi.withProvider(...). Thats it as simple as that to switch between Vendors.
  
  - Though the Point Of Interest is Seattle, WA, The app is developed such a way it can take any place of interest or user's location 
  itself -- But due to time constraint, left without implementation.
  
  - And one more surpraise thing is the equivallent iOS product code is 50% ready (https://github.com/pavan2you/HomeAwayPlacesIOS). 
  You can just traverse the code, no output. It clearly conveys if a developer approach is scalable, developing same requirements
  for android/iOS is pretty same and can gain beneifits of sharing code. 
  
  I've started this execrcise on 27th May 2018, 11:45 (9hrs) then after that every day 4-5hrs, roughly I've taken 25hrs for 
  Android + 0.5 iOS and ~7500 line of code. 
