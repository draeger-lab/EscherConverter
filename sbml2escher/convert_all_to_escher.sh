#!/usr/bin/env bash
#
# convert_all_to_escher.sh
# ------------------------
# Converts each mature map listed in MAPS.md from CellDesigner XML to Escher JSON,
# following https://escher.readthedocs.io/en/stable/escherconverter.html (section 8.2,
# "CellDesigner conversion") using the bundled sbml2escher.py script.
#
# HOW IT WORKS
#   sbml2escher.py POSTs each CellDesigner file to the MINERVA public conversion
#   service (https://minerva-service.lcsb.uni.lu) to obtain SBML+layout, then builds
#   the Escher JSON locally. => An internet connection able to reach that service is
#   REQUIRED. (It could not be run inside the Cowork sandbox, whose network is locked
#   down, so run this on your own machine.)
#
# REQUIREMENTS
#   - Python 3 with: pip install xmltodict requests
#
# USAGE
#   bash code/EscherConverter/sbml2escher/convert_all_to_escher.sh
#   (runs from anywhere; input paths are resolved against the metabolicCartography
#    project root, which is three levels above this script.)
#
# Outputs land in this script's json/ folder. Large maps (whole ReconMap, ReconMap 2.0)
# can take several minutes each on the MINERVA service.

set -u

# Resolve the metabolicCartography project root: three levels up from this script
# (sbml2escher -> EscherConverter -> code -> metabolicCartography).
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
OUT="$SCRIPT_DIR/json"
TMP="$SCRIPT_DIR/.tmp_inputs"
mkdir -p "$OUT" "$TMP"

CONVERT="$SCRIPT_DIR/sbml2escher.py"

# map "output_name|relative/input/path" (input paths may contain spaces)
maps=(
  "reconMap3_whole|results/2017_ReconMap/maps/FullMap/mergeNewReconMap/WholeMap/20170901_reconMap/Final_map/reconMap2_allin_FBA.xml"
  "organelle_mitochondrion|results/2017_ReconMap/maps/FullMap/Organelles_2017/organellesFinal/mito_R.xml"
  "organelle_endoplasmic_reticulum|results/2017_ReconMap/maps/FullMap/Organelles_2017/organellesXml/20170809_ReticulumFinal.xml"
  "organelle_peroxisome|results/2017_ReconMap/maps/FullMap/Organelles_2017/organellesFinal/perox_R.xml"
  "organelle_lysosome|results/2017_ReconMap/maps/FullMap/Organelles_2017/organellesFinal/lysosome_R.xml"
  "organelle_golgi|results/2017_ReconMap/maps/FullMap/Organelles_2017/organellesFinal/golgi_R.xml"
  "organelle_nucleus|results/2017_ReconMap/maps/FullMap/Organelles_2017/organellesFinal/nucleus_R.xml"
  "mitomap_metabolic|results/2017_MitoMap/metabolicMaps/mitoFinal2017/20170809_MitochondrialMap_FINAL.xml"
  "mitomap_signalling|results/2017_MitoMap/signalling/maps/mitomap_default_complete.xml"
  "iDN1_NN_map|results/iDN1map/results/NN main map 3.6- final map.xml"
  "reconMap2_recon2|results/2013_results/2013_ReconMap/ReconMap33.33.xml"
  "cobra_v3_submodel|results/2017_cobraV3PaperSubmodel/graphs/COBRA2018/COBRAimage.xml"
)

ok=0; fail=0
for entry in "${maps[@]}"; do
  name="${entry%%|*}"
  rel="${entry#*|}"
  src="$ROOT/$rel"
  echo "==================================================================="
  echo ">> $name"
  if [[ ! -f "$src" ]]; then
    echo "   MISSING input: $src"; fail=$((fail+1)); continue
  fi
  # The docs advise no spaces in filenames: stage a space-free copy.
  safe="$TMP/${name}.xml"
  cp "$src" "$safe"
  if python3 "$CONVERT" --input="$safe" --output="$OUT/${name}.json"; then
    ok=$((ok+1))
  else
    echo "   conversion FAILED for $name"; fail=$((fail+1))
  fi
  rm -f "$safe"
done

rmdir "$TMP" 2>/dev/null || true
echo "==================================================================="
echo "Done. Success: $ok  Failed: $fail   Output: $OUT"
