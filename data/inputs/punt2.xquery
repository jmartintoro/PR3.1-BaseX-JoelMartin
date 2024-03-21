declare option output:method "xml";
declare option output:indent "yes";

<users>{
  for $u in /users/row
  let $posts := count(/posts/row[@OwnerUserId=$u/@Id and @PostTypeId="1"])
  order by $posts descending
  return <user>
    <name>{$u/@DisplayName/string()}</name>
    <postAmount>{$posts}</postAmount>
  </user>
}</users>
