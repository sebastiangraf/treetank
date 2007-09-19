for $i in /site//item
where contains($i/description, "gold")
return $i/name/text()
