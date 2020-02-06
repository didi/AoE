import setuptools

with open("README.md", "r") as fh:
    long_description = fh.read()

setuptools.setup(
    name="aoe-tool",
    version="0.0.3",
    author="zouyuefu",
    author_email="zouyf_1984@163.com",
    description="aoe tools.",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/didi/AoE",
    packages=setuptools.find_packages(),
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: Apache Software License",
        "Operating System :: OS Independent",
    ],
    entry_points={
        'console_scripts': ['atenc=encrypt.cmd:main'],
    },
    python_requires='>=3.6',
)