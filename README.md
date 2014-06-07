# mongato

An extention to Monger:

Adds metainfo to maps returned by monger for custimized rendering.

## Example     

### Using Monger

    mongato.core=> (mc/save-and-return "people" { :_id (ObjectId.) :uuid (mongato.util/uuid) :first_name "John" :last_name "Lennon" }) 
    (mc/save-and-return "people" { :_id (ObjectId.) :uuid (mongato.util/uuid) :first_name "John" :last_name "Lennon" })
    {:last_name "Lennon", :first_name "John", :uuid "98a77f4dc328f58a26b3bdf72630b209cbb8e1c1", :_id #<ObjectId 53937508036402735f5a7b2a>}
    mongato.core=> (mc/find-maps "people" {})
    ( {:_id #<ObjectId 53937508036402735f5a7b2a>, :uuid "98a77f4dc328f58a26b3bdf72630b209cbb8e1c1", :first_name "John", :last_name "Lennon"})


### Using Monger + Mongato

    (connect-from-settings "mongodb-config.clj")
    mongato.core=> (defdata people :hide :_id :by-name :uuid mongato.render/render-last4)
    #'mongato.core/people
    mongato.core=> (save-and-return-tmap people { :_id (ObjectId.) :uuid (mongato.util/uuid) :first_name "John" :last_name "Lennon" })
    {:last_name "Lennon", :first_name "John", :uuid "193c772239236d57a7d45de5586e79de44929b18", :_id #<ObjectId 539377270364c0701d6697d6>}
    mongato.core=> (printm (find-tmaps people))
    ({:last_name Lennon, :first_name John, :uuid ..dc6b})
    
## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
