for $i in /site/regions/australia/item
return <item name="{ $i/name/text() }">{ $i/description }</item>

