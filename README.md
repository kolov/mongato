# mongato

An extention to Monger:

Adds metainfo to maps returned by monger for custimized rendering.

## Example     

### Using Monger

```clojure
; Connect to MongoDB and authenticate
; Insert a new document the usual way
mongato.core=> (mc/save-and-return "people" { :_id (ObjectId.) :uuid (mongato.util/uuid) 
    :first_name "John" :last_name "Lennon" }) 
; See what objects are in 'people' 
mongato.core=> (mc/find-maps "people" )
( {:_id #<ObjectId 53937508036402735f5a7b2a>, :uuid "98a77f4dc328f58a26b3bdf72630b209cbb8e1c1", 
:first_name "John", :last_name "Lennon"})
; A bit too verbose, a lot of information I don't need to see here
```

### Using Monger + Mongato

```clojure
; Connect from setttings in a file
mongato.core=>(connect-from-settings "mongodb-config.clj")
; define the data 
mongato.core=> (defdata people :hide :_id :by-name :uuid mongato.render/render-last4)
; Insert a object
mongato.core=> (save-and-return-tmap people { :_id (ObjectId.) :uuid (mongato.util/uuid) 
    :first_name "John" :last_name "Lennon" })
; See what is there
mongato.core=> (printm (find-tmaps people))
({:last_name Lennon, :first_name John, :uuid ..dc6b})
; the object rendered as defined in defdata
```
    
## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
