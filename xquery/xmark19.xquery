for    $b in /site/regions//item
let    $k := $b/name/text()
order by $k
return <item name="{ $k }">{ $b/location/text() }</item>
