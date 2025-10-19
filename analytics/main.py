import yaml
from pathlib import Path

# Single YAML document
path = Path("data", "test.yaml")
data = yaml.safe_load(path.read_text(encoding="utf-8"))
print(data['parser'])
#print(data['originalCode'])
print(data['request']['brokenSnippet'])

