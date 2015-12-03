# Process Targets
    
# Open the targets text file in read mode
f = open('targets.txt', 'r')

# For every target
for line in f:
    # measure the hops/ get info for that target
    if not line.startswith("#"):
        measure_info(line.split()[0])
# Close the target file
f.close()