
# Glide Transformations

Useful Glide transformations for use with the Android Glide image loading library.

## Getting Started

### Prerequisites

Android development environment with the jcenter repository added in the project's build.gradle file.
A target SDK of 14 or higher.

```
repositories {
        jcenter()
    }
```

### Installation

Add the following dependency in the app's build.gradle file:

```java
dependencies {
    implementation 'net.scarlettsystems.android:glide:0.0.3'
}
```

## Transformations

### Preview

| Transformation        | Image           | Description  |
| ------------- |:-------------------:| ---|
| Original      | <img src="https://raw.githubusercontent.com/shanescarlett/Glide-Transformations/master/samples/Original.png" width="60%"/>| The original image. |
| Greyscale      | <img src="https://raw.githubusercontent.com/shanescarlett/Glide-Transformations/master/samples/Greyscale.png" width="60%" />| Desaturates image and produces a greyscale result. |
| GaussianBlur      | <img src="https://raw.githubusercontent.com/shanescarlett/Glide-Transformations/master/samples/GaussianBlur.png" width="60%" />| Applies RenderScript Gaussian blur with specified radius. |
| Mosaic      | <img src="https://raw.githubusercontent.com/shanescarlett/Glide-Transformations/master/samples/Mosaic.png" width="60%" />| Applies a mosaic or pixellation effect. |
| Padding      | <img src="https://raw.githubusercontent.com/shanescarlett/Glide-Transformations/master/samples/Padding.png" width="60%" />| Adds padding intrinsically to the Bitmap. Useful to use in conjunction with Shadow to prevent clipping. |
| Ellipse      | <img src="https://raw.githubusercontent.com/shanescarlett/Glide-Transformations/master/samples/Ellipse.png" width="60%" />| Crops image by specified ellipse and angle. Can be used to crop a perfect circle.|
| Shadow      | <img src="https://raw.githubusercontent.com/shanescarlett/Glide-Transformations/master/samples/Shadow.png" width="60%" />| Adds shadow intrinsically to the Bitmap. Useful for complex shapes where Android cannot render an elevation shadow by default. |

### Usage

Transformations can be passed into Glide using standard methods:

```Java
Glide.with(this)
	.load(R.drawable.image)
	.apply(new RequestOptions().transform(new Ellipse(this)))
	.into(imageView);
```
Or multiple transformations at once:
```Java
Glide.with(this)
	.load(R.drawable.image)
	.apply(new RequestOptions().transforms(
		new Ellipse(this),
		new Mosaic(this).setByWidth(10),
		new Greyscale(this)
	))
	.into(imageView);
```
Some transformations have parameters that can be set via builder notation:
```Java
Transformation t = new Shadow(this).setBlurRadius(10).setElevation(10).setAngle(45);
```

### Notes
* Glide processes transformations in the order they are given as parameters to the `transforms()` function. It is important to consider the application order to achieve your desired effect. For instance, applying a cropping transformation (e.g. `Padding()` or `Ellipse()`) before `GaussianBlur()` will blur the newly drawn edges as well. It may be desired to apply a crop afterwards in order to preserve sharp image boundaries.
* It is advised to perform image scaling through Glide's own transformations such as `CenterCrop()` and `FitCenter()`  **before any other transformations** rather than specifying a `scaleType` in the `ImageView`'s XML. Not only is it faster, but transformations do not know the final scaling mode and cannot adjust the transformation accordingly. Processing the scaling in XML may have undesired interactions with the transformations. 

## Versioning

Current version: 0.0.3

## Authors

* **Shane Scarlett** - *core development* - [Scarlett Systems](https://scarlettsystems.net)


## License

This project is licensed under the Apache 2.0 License.
