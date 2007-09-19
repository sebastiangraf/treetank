for    $p in /site/people/person
where  empty($p/homepage/text())
return <person name="{ $p/name/text() }"/>

