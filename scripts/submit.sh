#!/bin/bash
function wrap_option() {
  local _option="$1"
  if [ -z "$_option" ] || [ "$_option" == "\"\"" ]; then
     echo
     return
  fi
  echo "$1"
}
_SCRIPT=$(mktemp)
cat > $_SCRIPT <<EOF
#$ -b y
#$ -sync y
#$ -o /opt/blast-wrapper/logs/out.log
#$ -e /opt/blast-wrapper/logs/err.log
module load BLAST+ && \
$(wrap_option "$1") \
    -query $(wrap_option "$2") \
    -db $(wrap_option "$3") \
    -out $(wrap_option "$4") \
    -outfmt "$(wrap_option "$5")" \
    $(wrap_option "$6") \
    $(wrap_option "$7") \
    $(wrap_option "$8") \
    $(wrap_option "$9") \
    $(wrap_option "${10}")
EOF
chmod +x $_SCRIPT
qsub $_SCRIPT
rm -f $_SCRIPT