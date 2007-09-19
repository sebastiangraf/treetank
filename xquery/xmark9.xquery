for $p in /site/people/person
let $a := for $t in /site/closed_auctions/closed_auction
          let $n := for $t2 in /site/regions/europe/item
          where $t/itemref/@item = $t2/@id
          return $t2
    where $p/@id = $t/buyer/@person
    return <item>{ $n/name/text() }</item>
return <person name="{ $p/name/text() }">{ $a }</person>
