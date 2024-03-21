declare option output:method "xml";
declare option output:indent "yes";

<tags>{
  for $t in /tags/row
  order by xs:integer($t/@Count) descending
  return <tag>
    <name>{$t/@TagName/string()}</name>
    <tagAmount>{$t/@Count/string()}</tagAmount>
  </tag>
}</tags>
