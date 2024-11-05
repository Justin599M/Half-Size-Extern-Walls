Version 0.6
This program is a post slicing script that cuts external perimeter lines into 2 per layer. In theory this maintains print quality while speeding this up.
Currently takes in 1 argument that is the filepath to a GCode file made by PrusaSlicer
This program is provided as is with no liability or warranty

Todo List:
  Change how program detects when we are not longer printing external perimeters
  Add argument for Z hop and retraction.
  Add argument for External Flow Rate.
  Add reversed mode: where the top half of an external line starts where the bottom half ended.


Known Issues:
  G1 X Y and Z values are off by a negligible amount
