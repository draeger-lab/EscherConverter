# Escher conversion of the metabolic maps

Converts each mature map in [`MAPS.md`](../../../MAPS.md) from CellDesigner XML to
[Escher](https://escher.github.io) JSON, following the official method in
<https://escher.readthedocs.io/en/stable/escherconverter.html> (§8.2, *CellDesigner conversion*).

## Contents

- `sbml2escher.py` — the official Escher conversion script (from `opencobra/escher`).
- `convert_all_to_escher.sh` — batch runner that converts all 12 maps in one command.
- `json/` — Escher JSON outputs, one per map.
- `xml/` — archived copies of the CellDesigner source `.xml`, each renamed to match its output.
- `sbgn/` — SBGN-ML export of the MitoMap signalling map.

## How it works

`sbml2escher.py` sends each CellDesigner file to the **MINERVA public conversion service**
(`https://minerva-service.lcsb.uni.lu`) to obtain SBML + layout, then builds the Escher JSON
locally. **An internet connection that can reach that service is required.** This is why the
conversion could not be executed inside the Cowork sandbox (its network is locked down) and
must be run on your own machine.

## Run it

```bash
# 1. install the two Python dependencies
pip install xmltodict requests

# 2. run the batch converter (input paths resolve to the project root automatically):
bash code/EscherConverter/sbml2escher/convert_all_to_escher.sh
```

Outputs are written to `json/<name>.json` next to this script, one per map:

| Output | Source map |
|--------|------------|
| `reconMap3_whole.json` | ReconMap 3 — whole network |
| `organelle_mitochondrion.json` | Mitochondrion |
| `organelle_endoplasmic_reticulum.json` | Endoplasmic reticulum |
| `organelle_peroxisome.json` | Peroxisome |
| `organelle_lysosome.json` | Lysosome |
| `organelle_golgi.json` | Golgi apparatus |
| `organelle_nucleus.json` | Nucleus |
| `mitomap_metabolic.json` | MitoMap — metabolic |
| `mitomap_signalling.json` | MitoMap — signalling |
| `iDN1_NN_map.json` | iDN1 / "NN" neuronal map |
| `reconMap2_recon2.json` | ReconMap 2.0 (Recon 2 lineage) |
| `cobra_v3_submodel.json` | COBRA v3 paper submodel |

## Notes

- The runner stages a space-free copy of each input first (the docs advise against spaces in
  filenames — relevant to `NN main map 3.6- final map.xml`).
- Large maps (`reconMap3_whole`, `reconMap2_recon2`) can take several minutes each on the
  MINERVA service; the script's HTTP timeout is 10 minutes per file.
- The endoplasmic reticulum uses `organellesXml/20170809_ReticulumFinal.xml`; the
  `organellesFinal/ret_R.xml` and `ret.xml` files are rejected by MINERVA (HTTP 400).
- To convert a single map manually:
  `python3 sbml2escher.py --input=path/to/map.xml --output=map.json`
- Load a resulting `.json` in the Escher builder at <https://escher.github.io> (Map → Load map JSON).
